/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package org.wltea.analyzer.core;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.ext.PinyinGenerator;

/**
 * IK分词器主类
 *
 */
public final class IKSegmenter {
	
	//字符窜reader
	private Reader input;
	//分词器配置项
	private Configuration cfg;
	//分词器上下文
	private AnalyzeContext context;
	//分词处理器列表
	private List<ISegmenter> segmenters;
	//分词歧义裁决器
	private IKArbitrator arbitrator;

	//是否分析拼音
	private boolean analysisPinyin=false;

	//当前的词元
	private Lexeme curChineseLexeme=null;

	//当前词元的拼音组合列表
	private List<String> pinyinLexemeList=null;

	//是否有拼音词元
	private boolean hasPinyin=false;

	/**
	 * IK分词器构造函数
	 * @param input 
	 * @param useSmart 为true，使用智能分词策略
	 * 
	 * 非智能分词：细粒度输出所有可能的切分结果
	 * 智能分词： 合并数词和量词，对分词结果进行歧义判断
	 */
	public IKSegmenter(Reader input , boolean useSmart){
		this.input = input;
		this.cfg = DefaultConfig.getInstance();
		this.cfg.setUseSmart(useSmart);
		this.init();
	}
	
	/**
	 * IK分词器构造函数
	 * @param input
	 * @param cfg 使用自定义的Configuration构造分词器
	 * 
	 */
	public IKSegmenter(Reader input , Configuration cfg){
		this.input = input;
		this.cfg = cfg;
		this.init();
	}

	public IKSegmenter(Reader input , boolean useSmart,boolean analysisPinyin){
		this(input,useSmart);
		this.analysisPinyin=analysisPinyin;
	}

	public IKSegmenter(Reader input , Configuration cfg,boolean analysisPinyin){
		this(input,cfg);
		this.analysisPinyin=analysisPinyin;
	}

	/**
	 * 初始化
	 */
	private void init(){
		//初始化词典单例
		Dictionary.initial(this.cfg);
		//初始化分词上下文
		this.context = new AnalyzeContext(this.cfg);
		//加载子分词器
		this.segmenters = this.loadSegmenters();
		//加载歧义裁决器
		this.arbitrator = new IKArbitrator();
	}
	
	/**
	 * 初始化词典，加载子分词器实现
	 * @return List<ISegmenter>
	 */
	private List<ISegmenter> loadSegmenters(){
		List<ISegmenter> segmenters = new ArrayList<ISegmenter>(4);
		//处理字母的子分词器
		segmenters.add(new LetterSegmenter()); 
		//处理中文数量词的子分词器
		segmenters.add(new CN_QuantifierSegmenter());
		//处理中文词的子分词器
		segmenters.add(new CJKSegmenter());
		return segmenters;
	}
	
	/**
	 * 分词，获取下一个词元
	 * @return Lexeme 词元对象
	 * @throws IOException
	 */
	public synchronized Lexeme next()throws IOException{

		//扩展英文分词的地方1
		if(analysisPinyin && hasPinyin) {
			int pinyinListCnt =pinyinLexemeList==null?0:pinyinLexemeList.size();
			if(pinyinListCnt<=0) {
				hasPinyin=false;
			}else {
				String curPinyin = pinyinLexemeList.get(0);
				Lexeme pinyinLexeme = new Lexeme(curChineseLexeme.getOffset(),curChineseLexeme.getBegin(),curChineseLexeme.getLength(),Lexeme.TYPE_LETTER);
				pinyinLexeme.setLexemeText(curPinyin);
				pinyinLexemeList.remove(0);
				return pinyinLexeme;
			}
		}

		Lexeme l = null;
		while((l = context.getNextLexeme()) == null ){
			/*
			 * 从reader中读取数据，填充buffer
			 * 如果reader是分次读入buffer的，那么buffer要  进行移位处理
			 * 移位处理上次读入的但未处理的数据
			 */
			int available = context.fillBuffer(this.input);
			if(available <= 0){
				//reader已经读完
				context.reset();
				return null;
				
			}else{
				//初始化指针
				context.initCursor();
				do{
        			//遍历子分词器
        			for(ISegmenter segmenter : segmenters){
        				segmenter.analyze(context);
        			}
        			//字符缓冲区接近读完，需要读入新的字符
        			if(context.needRefillBuffer()){
        				break;
        			}
   				//向前移动指针
				}while(context.moveCursor());
				//重置子分词器，为下轮循环进行初始化
				for(ISegmenter segmenter : segmenters){
					segmenter.reset();
				}
			}
			//对分词进行歧义处理
			this.arbitrator.process(context, this.cfg.useSmart());			
			//将分词结果输出到结果集，并处理未切分的单个CJK字符
			context.outputToResult();
			//记录本次分词的缓冲区位移
			context.markBufferOffset();			
		}

		//扩展英文分词的地方2
		if(analysisPinyin) {
			String lexemeStr =l==null?null:l.getLexemeText();
			if(lexemeStr!=null && hasChinese(lexemeStr)) {
				List<String> pyList = PinyinGenerator.genPinyinList(lexemeStr);
				int pyCnt = pyList==null?0:pyList.size();
				if(pyCnt>0) {
					hasPinyin=true;
					curChineseLexeme=l;
					pinyinLexemeList=pyList;
				}
			}
		}

		return l;
	}

	/**
     * 重置分词器到初始状态
     * @param input
     */
	public synchronized void reset(Reader input) {
		this.input = input;
		context.reset();
		for(ISegmenter segmenter : segmenters){
			segmenter.reset();
		}
	}


	public static boolean hasChinese(String str)
	{
		if(str==null)
		{
			return false;
		}
		char[] charArray = str.toCharArray();
		for (int i = 0; i < charArray.length; i++)
		{
			if (isChinese(charArray[i]))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isChinese(char str)
	{
		if ((str >= 0x4e00) && (str <= 0x9fbb))
		{
			return true;
		}
		return false;
	}
}
