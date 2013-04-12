/*
 * @(#)JavacOption.java	1.3 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.main;

import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Version;
import java.io.PrintWriter;

/**
 * TODO: describe com.sun.tools.javac.main.JavacOption
 *
 * <p><b>This is NOT part of any API supported by Sun Microsystems.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 */
@Version("@(#)JavacOption.java	1.3 07/03/21")
public interface JavacOption {
	
    OptionKind getKind();

    /** Does this option take a (separate) operand? */
    boolean hasArg();

    /** Does argument string match option pattern?
     *  @param arg        The command line argument string.
     */
    boolean matches(String arg);

    /** Process the option (with arg). Return true if error detected.
     */
    boolean process(Options options, String option, String arg);

    /** Process the option (without arg). Return true if error detected.
     */
    boolean process(Options options, String option);
    
    OptionName getName();

    enum OptionKind {
        NORMAL,  //��׼ѡ��
        EXTENDED,//�Ǳ�׼ѡ��(Ҳ����չѡ��,�ñ�׼ѡ�-X�����鿴������չѡ��)
        HIDDEN,  //����ѡ��(�ڲ�ʹ�ã�������ʾ)
    }
    
    /** This class represents an option recognized by the main program
     */
    static class Option implements JavacOption {

	/** Option string.
	 */
	OptionName name;

	/** Documentation key for arguments.
	 */
	String argsNameKey;

	/** Documentation key for description.
	 */
	String descrKey;

	/** Suffix option (-foo=bar or -foo:bar)
	 */
	boolean hasSuffix; //ѡ���������һ���ַ���'=' �� ':'
	
	/*
	argsNameKey��descrKey��Documentation������������ļ���:
	com\sun\tools\javac\resources\javac.properties(�ֹ��ʻ��汾)
	
	��:-classpath <·��> ָ�������û����ļ���ע�ʹ��������λ��
	OptionName name    ��ӦCLASSPATH     (-classpath);
	String argsNameKey ��Ӧopt.arg.path  (<·��>);
	String descrKey    ��Ӧopt.classpath (ָ�������û����ļ���ע�ʹ��������λ��); 
	
	
	��RecognizedOptions���getAll()�����ﰴ�ո���
	��������������Option(������������:XOption��HiddenOption)
	*/
	Option(OptionName name, String argsNameKey, String descrKey) {
	    this.name = name;
	    this.argsNameKey = argsNameKey;
	    this.descrKey = descrKey;
	    char lastChar = name.optionName.charAt(name.optionName.length()-1);
	    hasSuffix = lastChar == ':' || lastChar == '=';
	}
	Option(OptionName name, String descrKey) {
	    this(name, null, descrKey);
	}

	public String toString() {
	    return name.optionName;
	}
	
	//�Ҽ��ϵģ�������;
	public String OptionDEBUG() {
		return "hasArg="+hasArg()+"    hasSuffix="+hasSuffix+
		"    kind="+getKind()+"    name="+name+
		"    argsNameKey="+argsNameKey+"    descrKey="+descrKey;
	}

	/** Does this option take a (separate) operand?
	 */
	public boolean hasArg() {
		/*
		ע���������һ������:û�к�׺��ѡ��(Ҳ����ѡ��������ѡ������ֿ�)
		����������ѡ���true:
		-sourcepath <·��>           ָ����������Դ�ļ���λ��
		-bootclasspath <·��>        �����������ļ���λ��
		
		����������ѡ���false:
		-Xbootclasspath:<·��>       �����������ļ���λ��
		-Djava.ext.dirs=<Ŀ¼>       ���ǰ�װ����չĿ¼��λ��
		*/
	    return argsNameKey != null && !hasSuffix;
	}

	/** Does argument string match option pattern?
	 *  @param arg        The command line argument string.
	 */
        public boolean matches(String arg) {
		//��-Xbootclasspath/a:src;classes������ѡ��
		//�ǲ���𿪳ɡ�-Xbootclasspath/a:���롰src;classes���ģ�
		//��ʱ��arg=-Xbootclasspath/a:src;classes
		//name.optionName=-Xbootclasspath/a:
		//hasSuffix=true
	    return hasSuffix ? arg.startsWith(name.optionName) : arg.equals(name.optionName);
	}

	/** Print a line of documentation describing this option, if standard.
	 */
	void help(PrintWriter out) {
	    String s = "  " + helpSynopsis();
	    out.print(s);
	    for (int j = s.length(); j < 29; j++) out.print(" ");
	    Log.printLines(out, Main.getLocalizedString(descrKey));
	}
	String helpSynopsis() {
	    return name +
		(argsNameKey == null ? "" :
		 ((hasSuffix ? "" : " ") +
		  Main.getLocalizedString(argsNameKey)));
	}

	/** Print a line of documentation describing this option, if non-standard.
	 */
	void xhelp(PrintWriter out) {}

	/** Process the option (with arg). Return true if error detected.
	 */
	public boolean process(Options options, String option, String arg) {
		//options�൱��һ��Map<K,V>�����Ժ�ĳ�������о����õ���
		//���Ȱ�keyȡֵ��Ȼ��ȡ����ֵ�Ƿ�Ϊnull������boolean������ֵ
            if (options != null)
                options.put(option, arg);
	    return false;
	}

	/** Process the option (without arg). Return true if error detected.
	 */
	public boolean process(Options options, String option) {
	    if (hasSuffix)
		return process(options, name.optionName, option.substring(name.optionName.length()));
	    else
		return process(options, option, option);
	}
        
        public OptionKind getKind() { return OptionKind.NORMAL; }
        
        public OptionName getName() { return name; }
    };

    /** A nonstandard or extended (-X) option
     */
    static class XOption extends Option {
	XOption(OptionName name, String argsNameKey, String descrKey) {
	    super(name, argsNameKey, descrKey);
	}
	XOption(OptionName name, String descrKey) {
	    this(name, null, descrKey);
	}
	void help(PrintWriter out) {}
	void xhelp(PrintWriter out) { super.help(out); }
        public OptionKind getKind() { return OptionKind.EXTENDED; }
    };

    /** A hidden (implementor) option
     */
    static class HiddenOption extends Option {
	HiddenOption(OptionName name) {
	    super(name, null, null);
	}
	HiddenOption(OptionName name, String argsNameKey) {
	    super(name, argsNameKey, null);
	}
	void help(PrintWriter out) {}
	void xhelp(PrintWriter out) {}
        public OptionKind getKind() { return OptionKind.HIDDEN; }
    };

}