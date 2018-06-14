package org.wltea.analyzer.ext;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.List;

public class PinyinGenerator {

    public static List<String> genPinyinList(String str) {
        List<String> list = new ArrayList<String>();
        String quanPin = PinyinHelper.convertToPinyinString(str, "", PinyinFormat.WITHOUT_TONE);
        if(quanPin!=null && quanPin.length()>0) {
            list.add(quanPin);
        }else {
            //全拼为空，直接返回
           return list;
        }
        String simplePin = PinyinHelper.getShortPinyin(str);
        int simpleCnt = simplePin==null?0:simplePin.length();
        if(simpleCnt>0) {
            list.add(simplePin);
        }
        int count = str.length();
        for(int i=1;i<count;i++) {
            String curStr = str.substring(0,i);
            String qPin = PinyinHelper.convertToPinyinString(curStr, "", PinyinFormat.WITHOUT_TONE);
            if(qPin==null || qPin.length()<=0) {
                continue;
            }
            //是否包含多音
            boolean includeMultYin=false;
            if(!quanPin.startsWith(qPin)) {
                includeMultYin=true;
            }
            if(includeMultYin) {
                String leftStr = str.substring(i);
                String leftPin = PinyinHelper.convertToPinyinString(leftStr, "", PinyinFormat.WITHOUT_TONE);
                if(leftPin!=null && quanPin.endsWith(leftPin)) {
                    qPin= quanPin.replaceAll(leftPin+"$","");
                }
            }
            list.add(qPin);

            if(simpleCnt>=i) {
                String sPin = simplePin.substring(0, i);
                list.add(sPin);
            }
        }
        return list;
    }

}
