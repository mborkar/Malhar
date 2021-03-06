/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.logs;

import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.OutputPortFieldAnnotation;
import com.datatorrent.lib.util.BaseLineTokenizer;
import com.datatorrent.lib.util.UnifierHashMap;

import java.util.HashMap;

/**
 *
 * Splits lines into tokens, and tokens into sub-tokens and emits key,val pairs in a HashMap. Useful to convert String (log lines) into a POJO (HashMap)<p>
 * This module is a pass through<br>
 * <br>
 * <b>StateFull : No, </b> tokens are processed in current window. <br>
 * <b>Partitions : Yes, </b>output unifier. <br>
 * <br>
 * <br>
 * Ideal for applications like log processing<br>
 * <b>Ports</b>:<br>
 * <b>data</b>: expects String<br>
 * <b>tokens</b>: emits HashMap&lt;String,String&gt;<br>
 * <br>
 * <b>Properties</b>:<br>
 * <b>splitby</b>: The characters used to split the line. Default is ";\t "<br>
 * <b>splittokenby</b>: The characters used to split a token into key,val pair. Default is "", i.e. tokens are not split, and key is set to token, and val is null<br>
 * <br>
 *
 * @since 0.3.2
 */
public class LineTokenizerKeyVal extends BaseLineTokenizer
{
  @OutputPortFieldAnnotation(name = "tokens")
  public final transient DefaultOutputPort<HashMap<String, String>> tokens = new DefaultOutputPort<HashMap<String, String>>()
  {
    @Override
    public Unifier<HashMap<String, String>> getUnifier()
    {
      return new UnifierHashMap<String, String>();  
    }
  };

  private transient HashMap<String, String> map = null;
  private transient String skey = "";
  private transient String sval = "";

  /**
   * sets up the cache
   */
  @Override
  public void beginProcessTokens()
  {
    map = new HashMap<String, String>();
  }

  /**
   * emits tokens on port "tokens", and clears the cache
   */
  @Override
  public void endProcessTokens()
  {
    if (map != null) {
      tokens.emit(map);
      map = null;
    }
  }

  /**
   * clears subtoken key,val pair
   */
  @Override
  public void beginProcessSubTokens()
  {
    skey = "";
    sval = "";
  }

  /**
   * inserts subtoken key,val pair in subtoken hash. If there are multiple keys with the same value
   * override this call and append values
   */
  @Override
  public void endProcessSubTokens()
  {
    if (!skey.isEmpty()) {
      map.put(skey, sval);
      skey = "";
      sval = "";
    }
  }

  /**
   * first subtoken is the key, the next is the val.
   * No error is flagged for third token as yet.
   * @param subtok
   */
  @Override
  public void processSubToken(String subtok)
  {
    if (skey.isEmpty()) {
      skey = subtok;
    }
    else if (sval.isEmpty()) {
      sval = subtok;
    }
    else {
      // emit error(?)
    }
  }
}
