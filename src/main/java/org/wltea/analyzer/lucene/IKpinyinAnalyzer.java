package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

//支持拼音的ik分析器
public class IKpinyinAnalyzer extends Analyzer {

    private boolean useSmart;

    public boolean useSmart() {
        return useSmart;
    }

    public void setUseSmart(boolean useSmart) {
        this.useSmart = useSmart;
    }

    /**
     * IK分词器Lucene  Analyzer接口实现类
     *
     * 默认细粒度切分算法
     */
    public IKpinyinAnalyzer(){
        this(false);
    }

    /**
     * IK分词器Lucene Analyzer接口实现类
     *
     * @param useSmart 当为true时，分词器进行智能切分
     */
    public IKpinyinAnalyzer(boolean useSmart){
        super();
        this.useSmart = useSmart;
    }

    /**
     * 重载Analyzer接口，构造分词组件
     */
//	@Override 4.x
//	protected TokenStreamComponents createComponents(String fieldName, final Reader in) {
//		Tokenizer _IKTokenizer = new IKTokenizer(in , this.useSmart());
//		return new TokenStreamComponents(_IKTokenizer);
//	}

	/* 5.x以上
	 * (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#createComponents(java.lang.String)
	 */
//	@Override
//	protected TokenStreamComponents createComponents(String fieldName) {
//		Reader in = null;
//        try{
//            in = new StringReader(fieldName);
//            Tokenizer it = new IKTokenizer(in , this.useSmart());
//            return new Analyzer.TokenStreamComponents(it);
//        } finally {
//        	if (in != null) {
//        		try {
//					in.close();
//				} catch (IOException e) {
//				}
//        	}
//        }
//	}

    protected Analyzer.TokenStreamComponents createComponents(String fieldName)
    {
        Tokenizer _IKTokenizer = new IKTokenizer(useSmart(),true);
        return new Analyzer.TokenStreamComponents(_IKTokenizer);
    }
}