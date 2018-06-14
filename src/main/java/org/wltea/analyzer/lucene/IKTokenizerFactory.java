package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

public class IKTokenizerFactory extends TokenizerFactory {
    private boolean useSmart;

    public boolean useSmart()
    {
        return this.useSmart;
    }

    public void setUseSmart(boolean useSmart) {
        this.useSmart = useSmart;
    }

    public IKTokenizerFactory(Map<String, String> args) {
        super(args);
        String useSmartArg = (String)args.get("useSmart");
        setUseSmart(useSmartArg != null ? Boolean.parseBoolean(useSmartArg) : false);
    }

    public Tokenizer create(AttributeFactory factory)
    {
        Tokenizer _IKTokenizer = new IKTokenizer(factory, this.useSmart);
        return _IKTokenizer;
    }
}