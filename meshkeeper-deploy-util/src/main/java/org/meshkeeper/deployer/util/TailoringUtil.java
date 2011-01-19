/**
 *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.meshkeeper.deployer.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Handy String file tailoring utilities. 
 * 
 * @author cmacnaug
 */
public class TailoringUtil {
  
  private boolean appendUnreplacedProperties = true;
  private boolean leaveReplacementComments = true;
  
  public static enum STYLE {
    SPRING, PROPFILE, ANT;

    public static STYLE toStyle(String name) {
      return valueOf(name.toUpperCase());
    }
  }

  STYLE style = STYLE.SPRING;

  public TailoringUtil() {
    
  }
  
  public TailoringUtil(STYLE style) {
    this.style = style;
  }

  public void setStyle(String style) {
    this.style = STYLE.toStyle(style);
  }

  public void setStyle(STYLE style) {
    this.style = style;
  }

  public STYLE getStyle() {
    return style;
  }

  /**
   * Tailors the given file with the specified properties. If the file is 
   * a directory then this method will recursively tailor all files 
   * in the directory.
   * 
   * @param f The file or directory. 
   * @param props The replacement properties. 
   * @throws IOException If there is an error tailoring.
   */
  public void tailorFile(File f, Properties props) throws IOException {
    if(f.isDirectory()) {
      
      for(File child : f.listFiles()) {
        tailorFile(child, props);
      }
      return;
    }
    
    switch (style) {
      case SPRING: {
        springScanAndReplace(f, props);
        break;
      }
      case PROPFILE: {
        propFileScanAndReplace(f, props, true, true);
        break;
      }
      case ANT: {
        antScanAndReplace(f, props);
        break;
      }
      default: { 
        throw new IllegalArgumentException("Unkown style: " + style);
      }
    }
  }

  /**
   * Same as {@link #springScanAndReplace(String, Properties)} except that the given {@link File} is
   * read in as a string and tailored. If substitutions are made then the file is rewritten with the
   * new content.
   * 
   * @return If no replacements are performed the same String object will be returned.
   * @throws IOException
   *           if there is an error reading the file.
   */
  public static String antScanAndReplace(File f, Properties props) throws IOException {
    String string = readFile(f);
    String tailored = springScanAndReplace(string, props);

    // If changes were made, then save the tailored file
    if (tailored != string) {
      writeFile(tailored, f);
    }

    return tailored;
  }

  /**
   * Scans the provided String looking for replacements sequences of the form <br>
   * <code>@toReplace@</code> <br>
   * replacing them with the value specified by "toReplace" in the provided properties if such a
   * property exists.
   * 
   * @return If no replacements are performed the same String object will be returned.
   */
  public static String antScanAndReplace(String string, Properties props) {

    return delimitedReplace(string, props, 0, "@", "@", false);
  }
  
  /**
   * Same as {@link #springScanAndReplace(String, Properties)} except that the given {@link File} is
   * read in as a string and tailored. If substitutions are made then the file is rewritten with the
   * new content.
   * 
   * @return If no replacements are performed the same String object will be returned.
   * @throws IOException
   *           if there is an error reading the file.
   */
  public static String springScanAndReplace(File f, Properties props) throws IOException {
    String string = readFile(f);
    String tailored = springScanAndReplace(string, props);

    // If changes were made, then save the tailored file
    if (tailored != string) {
      writeFile(tailored, f);
    }

    return tailored;
  }

  /**
   * Scans the provided String looking for replacements sequences of the form <br>
   * <code>${toReplace}</code> <br>
   * replacing them with the value specified by "toReplace" in the provided properties if such a
   * property exists. This replacement method also allows for default values to be specified in the
   * form: <br>
   * <code>${toReplace,defaultValue}</code> <br>
   * if no property called "toReplace" exists then <code>${toReplace,defaultValue}</code> will be
   * replaced with <code>defaultValue"</code>.
   * 
   * @return If no replacements are performed the same String object will be returned.
   */
  public static String springScanAndReplace(String string, Properties props) {

    return delimitedReplace(string, props, 0, "${", "}", true);
  }

  /**
   * Convenience method equivalent to
   * {@link #propFileScanAndReplace(String, Properties, boolean, boolean)} except that the
   * {@link File} is read in as a string and tailored. If substitutions are made then the file is
   * rewritten with the new content.
   * 
   * @param propFileContent
   *          The property file content
   * @param props
   *          The replacementProps properties
   * @param boolean appendUnreplacedValues if True values that are not replaced are appended to the
   *        end of the file.
   * 
   * @return If no replacements are performed the same String object will be returned.
   * @throws IOException
   *           if there is an error reading the file.
   */
  public static String propFileScanAndReplace(File f, Properties props,
      boolean appendUreplacedValues, boolean leaveReplaceComment) throws IOException {
    String string = readFile(f);
    String tailored = propFileScanAndReplace(readFile(f), props, appendUreplacedValues,
        leaveReplaceComment);

    // If changes were made, then save the tailored file
    if (tailored != string) {
      writeFile(tailored, f);
    }

    return tailored;
  }

  private static String delimitedReplace(String string, Properties props, final int start, final String openString, final String closeString, boolean commaDefaulted) {
    int open = string.indexOf(openString, start);
    int close = open == -1 ? -1 : string.indexOf(closeString, open);
    
    //See if there is a replacement candidate:
    if (open == -1 || close == -1) {
      return string;
    }

    String toReplace = null;
    //Recurse to handle the next replacement candidate first which
    //will tailor the remaining string and handled nested values such
    //as ${prop1${prop2${prop3}}}
    int next = string.indexOf(openString, open + openString.length());
    if (next > -1) {
      String tailored = delimitedReplace(string, props, next, openString, closeString, commaDefaulted);
      
      //If we did nested replacements make sure that the close hasn't changed:
      if(tailored != string) {
        close = tailored.indexOf(closeString, open + openString.length());
        //If the resulting tailoring left us with no close then simply
        //append it and break
        if(close == -1) {
          return tailored;
        } 
      }
      
      string = tailored;
    } 
    
    toReplace = string.substring(open + openString.length(), close);
    
    String replacement = null;
    // Check for a default:
    int lastComma = -1;
    if (commaDefaulted && (lastComma = toReplace.lastIndexOf(',')) > -1) {
      if (toReplace.length() > lastComma + 1) {
        replacement = toReplace.substring(lastComma + 1);
      }
      toReplace = toReplace.substring(0, lastComma);
    }

    replacement = props.getProperty(toReplace, replacement);
    if (replacement != null) {
      StringBuffer rc = new StringBuffer(string.length() + 1024);
      rc.append(string.substring(0, open));
      rc.append(replacement);
      rc.append(string.substring(close + closeString.length()));

      return rc.toString();
    } else {
      // Otherwise no replacement, leave untouched:
      return string;
    }
    
  }
  
  /**
   * Scans the provided String as a properties file looking for lines of the form <br>
   * <code>propName=propVal</code> <br>
   * replacing them with the value in the provided properties if such a property exists.
   * 
   * Lines starting with '#' or ';' are treated as comments and not tailored. <br>
   * This method preserves the original order of the properties file and comments.
   * 
   * @param propFileContent
   *          The propety file content
   * @param props
   *          The replacementProps properties
   * @param boolean appendUnreplacedValues if True values that are not replaced are appended to the
   *        end of the file.
   * 
   * @return If no replacements are performed the same String object will be returned.
   */
  public static String propFileScanAndReplace(String propFileContent, Properties replacementProps,
      boolean appendUreplacedValues, boolean leaveReplaceComment) {
    StringTokenizer tok = new StringTokenizer(propFileContent, "\r\n", true);
    StringBuffer rc = new StringBuffer(propFileContent.length() + 1024);
    Properties toAppend = null;
    if (appendUreplacedValues) {
      toAppend = new Properties();
      toAppend.putAll(replacementProps);
    }

    boolean replaced = false;
    int ePos = -1;
    //preserve newline style:
    String newline = propFileContent.indexOf("\r\n") == -1 ? "\n" : "\r\n";
    while (tok.hasMoreTokens()) {
      String token = tok.nextToken();
      if (token.length() == 1 && (token.equals("\r") || token.equals("\n"))) {
        rc.append(token);
        continue;
      }
      // Skip comments or lines without '=':
      if (isPropFileComment(token) || (ePos = token.indexOf("=")) == -1) {
        rc.append(token);
      } else {
        String propName = token.substring(0, ePos);
        String propVal = replacementProps.getProperty(propName);
        if (propVal != null) {
          if (leaveReplaceComment) {
            String oldVal = token.substring(ePos + 1).trim();
            //Don't need to comment if we aren't changing:
            if(!oldVal.equals(propVal)){
              rc.append('#');
              rc.append(token);
              rc.append(newline);
            }
          }
          rc.append(propName);
          rc.append('=');
          rc.append(replacementProps.getProperty(propName));
          if (toAppend != null) {
            toAppend.remove(propName);
          }
          replaced = true;
        } else {
          rc.append(token);
        }
      }
    }

    if (toAppend != null && !toAppend.isEmpty()) {
      rc.append(newline);
      rc.append(newline);
      rc.append("# Added by TailoringUtil:");
      for (Map.Entry<Object, Object> entry : toAppend.entrySet()) {
        rc.append(newline);
        rc.append(entry.getKey().toString());
        rc.append('=');
        rc.append(entry.getValue());
        replaced = true;
      }
    }

    if (!replaced) {
      return propFileContent;
    } else {
      return rc.toString();
    }
  }

  public static final boolean isPropFileComment(String line) {
    return line != null && line.startsWith("#") || line.startsWith(";");
  }

  private static void writeFile(String tailored, File f) throws IOException {
    FileWriter fw = new FileWriter(f, false);
    fw.write(tailored);
    fw.close();
  }

  private static String readFile(File f) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(f));
    ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

    int b = -1;
    while ((b = br.read()) != -1) {
      baos.write(b);
    }
    baos.close();
    return new String(baos.toByteArray());

  }

  /**
   * Indicates that unreplaced properties should be appended at the end of the file
   * being tailored. 
   * <p>
   * Applies to {@link STYLE#PROPFILE} tailoring.
   * @param appendUnreplacedProperties
   */
  public void setAppendUnreplacedProperties(boolean appendUnreplacedProperties) {
    this.appendUnreplacedProperties = appendUnreplacedProperties;
  }

  /**
   * @return Whether unreplaced properties should be appended at the end of the file
   * being tailored. 
   */
  public boolean isAppendUnreplacedProperties() {
    return appendUnreplacedProperties;
  }
  
  /**
   * Indicates that when a property line is replaced a comment containing
   * the original line should be left in the file. 
   * <p>
   * Applies to {@link STYLE#PROPFILE} tailoring.
   * @param appendUnreplacedProperties
   */
  public void setLeaveReplacementComments(boolean leaveReplacementComments) {
    this.leaveReplacementComments = leaveReplacementComments;
  }

  /**
   * @return Whether when a property line is replaced a comment containing
   * the original line should be left in the file. 
   */
  public boolean isLeaveReplacementComments() {
    return leaveReplacementComments;
  }
  


  public static final void main(String [] args) throws Exception {
    String style = args[0];
    File file = new File(args[1]);
    Properties props = new Properties();
    props.load(new FileInputStream(args[2]));
    
    TailoringUtil util = new TailoringUtil();
    util.setStyle(style);
    util.tailorFile(file, props);
  }
  
}
