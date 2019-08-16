/*
 * The MIT License
 *
 * Copyright 2019 wkgcass (https://github.com/wkgcass)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package vjson.parser;

import vjson.CharStream;
import vjson.JSON;
import vjson.Parser;
import vjson.ex.JsonParseException;
import vjson.ex.ParserFinishedException;
import vjson.simple.SimpleObject;
import vjson.simple.SimpleObjectEntry;

import java.util.LinkedList;
import java.util.List;

public class ObjectParser extends CompositeParser implements Parser<JSON.Object> {
    private final ParserOptions opts;
    private int state; // 0->`{`,1->first-key_or_`}`,2->`:`,3->value,4->`,`_or_`}`,5->key,6->finish,7->already_returned
    private LinkedList<SimpleObjectEntry<JSON.Instance>> map;
    private StringParser keyParser;
    private String currentKey;
    private Parser valueParser;

    public ObjectParser() throws NullPointerException, IllegalArgumentException {
        this(ParserOptions.DEFAULT);
    }

    public ObjectParser(ParserOptions opts) throws NullPointerException {
        super(opts);
        if (opts == null) {
            throw new NullPointerException();
        }
        this.opts = ParserOptions.ensureNotModifiedByOutside(opts);
        reset();
    }

    void reset() {
        state = 0;
        map = new LinkedList<>();
        keyParser = null;
        currentKey = null;
        valueParser = null;
    }

    public List<SimpleObjectEntry<JSON.Instance>> getMap() {
        return map;
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(String key) throws NullPointerException, IllegalArgumentException {
        if (key == null) {
            throw new NullPointerException();
        }
        this.currentKey = key;
    }

    private void handleKeyParser(boolean tryGetNewParser, CharStream cs, boolean isComplete) {
        try {
            if (keyParser == null) {
                if (tryGetNewParser) {
                    keyParser = getStringParser();
                } else {
                    return;
                }
            }
            JSON.String ret = keyParser.build(cs, isComplete);
            if (ret != null) {
                state = 2;
                currentKey = ret.toJavaObject();
                keyParser = null;
                opts.getListener().onObjectKey(this, currentKey);
            }
            // otherwise exception would be thrown or cs.hasNext() would return false
        } catch (JsonParseException e) {
            String err = "invalid json object: failed when parsing key: (" + e.getMessage() + ")";
            throw ParserUtils.err(opts, err);
        }
    }

    private void handleValueParser(boolean tryGetNewParser, CharStream cs, boolean isComplete) {
        try {
            if (valueParser == null) {
                if (tryGetNewParser) {
                    valueParser = getSubParser(cs);
                } else {
                    return;
                }
            }
            JSON.Instance inst = valueParser.build(cs, isComplete);
            if (inst != null) {
                state = 4;
                String key = this.currentKey;
                valueParser = null;
                this.currentKey = null;
                map.add(new SimpleObjectEntry<>(key, inst));
                opts.getListener().onObjectValue(this, key, inst);
            }
            // otherwise exception would be thrown or cs.hasNext() would return false
        } catch (JsonParseException e) {
            throw new JsonParseException("invalid json object: failed when parsing value: (" + e.getMessage() + ")");
        }
    }

    private boolean tryParse(CharStream cs, boolean isComplete) {
        // handle sub parser first if it exists
        handleKeyParser(false, cs, isComplete);
        handleValueParser(false, cs, isComplete);

        char c;
        String err;
        if (state == 0) {
            cs.skipBlank();
            if (cs.hasNext()) {
                opts.getListener().onObjectBegin(this);
                c = cs.moveNextAndGet();
                if (c == '{') {
                    state = 1;
                } else {
                    err = "invalid character for json object: not starts with `{`: " + c;
                    throw ParserUtils.err(opts, err);
                }
            }
        }
        if (state == 1) {
            cs.skipBlank();
            if (cs.hasNext()) {
                char peek = cs.peekNext();
                if (peek == '}') {
                    cs.moveNextAndGet();
                    state = 6;
                } else {
                    handleKeyParser(true, cs, isComplete);
                }
            }
        }
        while (cs.hasNext()) {
            if (state == 2) {
                cs.skipBlank();
                if (cs.hasNext()) {
                    c = cs.moveNextAndGet();
                    if (c != ':') {
                        err = "invalid key-value separator for json object, expecting `:`, but got " + c;
                        throw ParserUtils.err(opts, err);
                    }
                    state = 3;
                }
            }
            if (state == 3) {
                cs.skipBlank();
                if (cs.hasNext()) {
                    handleValueParser(true, cs, isComplete);
                }
            }
            if (state == 4) {
                cs.skipBlank();
                if (cs.hasNext()) {
                    c = cs.moveNextAndGet();
                    if (c == '}') {
                        state = 6;
                    } else if (c == ',') {
                        state = 5;
                    } else {
                        err = "invalid character for json object, expecting `}` or `,`, but got " + c;
                        throw ParserUtils.err(opts, err);
                    }
                }
            }
            if (state == 5) {
                cs.skipBlank();
                if (cs.hasNext()) {
                    handleKeyParser(true, cs, isComplete);
                }
            }
            if (state == 6) {
                break;
            }
            if (state == 7) {
                break;
            }
        }
        if (state == 6) {
            ++state;
            return true;
        } else if (state == 7) {
            cs.skipBlank();
            if (cs.hasNext()) {
                throw new ParserFinishedException();
            }
            return false;
        } else if (isComplete) {
            err = "expecting more characters to build object";
            throw ParserUtils.err(opts, err);
        } else {
            return false;
        }
    }

    @Override
    public JSON.Object build(CharStream cs, boolean isComplete) throws NullPointerException, JsonParseException, ParserFinishedException {
        if (cs == null) {
            throw new NullPointerException();
        }
        if (tryParse(cs, isComplete)) {
            opts.getListener().onObjectEnd(this);
            SimpleObject ret = new TrustedSimpleObject(map, TrustedFlag.FLAG);
            opts.getListener().onObject(ret);

            ParserUtils.checkEnd(cs, opts, "object");
            return ret;
        } else {
            return null;
        }
    }

    private static class TrustedSimpleObject extends SimpleObject {
        TrustedSimpleObject(List<SimpleObjectEntry<JSON.Instance>> map, TrustedFlag flag) {
            super(map, flag);
        }
    }
}
