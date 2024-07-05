package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Zheng Baoqiang on 7/5/2024.
 * Copyright (c) 2024 Zheng Baoqiang
 */

public class CoffeescriptLanguage implements LanguageBuilder {
  private static String[] ALIASES = { "coffee" };
  private static String IDENT_RE = "[A-Za-z$_][0-9A-Za-z$_]*";
  private static String POSSIBLE_PARAMS_RE = "(\\(.*\\)\\s*)?\\B[-=]>";
  private static String[] KEYWORDS_COFFEE = {
    "then",
    "unless",
    "until",
    "loop",
    "by",
    "when",
    "and",
    "or",
    "is",
    "isnt",
    "not"
  };
  private static String[] KEYWORDS_NOT_VALID = {
    "var",
    "const",
    "let",
    "function",
    "static"
  };
  private static String[] BUILT_INS = {
    "npm",
    "print"
  };
  private static String[] LITERALS = {
    "yes",
    "no",
    "on",
    "off"
  };
  private static Keyword[] KEYWORDS = new Keyword[] {
    new Keyword("keyword",  ArrayJavaMethods.exclude(ArrayJavaMethods.concat(ECMAScript.KEYWORDS, KEYWORDS_COFFEE), KEYWORDS_NOT_VALID)),
    new Keyword("built_in", ArrayJavaMethods.concat(ECMAScript.BUILT_INS, BUILT_INS)),
    new Keyword("literal",  ArrayJavaMethods.concat(ECMAScript.LITERALS,  LITERALS))
  };
  private static Mode SUBST  =
    new Mode().className("subst").begin("#\\{").end("\\}").keywords(KEYWORDS);

  private static Mode NUMBER =
    Mode.inherit(Mode.C_NUMBER_MODE, new Mode().starts(new Mode().end("(\\s*/)?").relevance(0)));

  private static Mode QUOTE_STRING =
    new Mode().className("string").begin("\"").end("\"").contains(new Mode[] {
      Mode.BACKSLASH_ESCAPE,
      SUBST,
    });
  private static Mode TRIPLE_APOS_STRING =
    new Mode().className("string").begin("'''").end("'''").contains(new Mode[] {
      Mode.BACKSLASH_ESCAPE,
    });
  private static Mode TRIPLE_QUOTE_STRING =
    new Mode().className("string").begin("\"\"\"").end("\"\"\"").contains(new Mode[] {
      Mode.BACKSLASH_ESCAPE,
      SUBST,
    });
  private static Mode STRING =
    new Mode().className("string").variants(new Mode[] {
      Mode.APOS_STRING_MODE,
      QUOTE_STRING,
      TRIPLE_APOS_STRING,
      TRIPLE_QUOTE_STRING,
    });
  private static Mode REGEXP =
    new Mode().className("regexp").variants(new Mode[] {
      new Mode().begin("///").end("///").contains(new Mode[] {
        SUBST,
        Mode.HASH_COMMENT_MODE,
      }),
      new Mode().begin("//[gim]{0,3}(?!\\w)").relevance(0),
      new Mode().begin("/(?![ *]).*?(?![\\\\])./[gim]{0,3}(?!\\w)")
    });
  private static Mode SUBLANG =
    new Mode().subLanguage("javascript").excludeBegin().excludeEnd().variants(new Mode[] {
      new Mode().begin("```").end("```"),
      new Mode().begin("`").end("`"),
    });
  private static Mode[] EXPRESSIONS = new Mode[] {
    Mode.BINARY_NUMBER_MODE,
    NUMBER,
    STRING,
    REGEXP,
    new Mode().begin("@" + IDENT_RE),
    SUBLANG,
  };
  private static Mode TITLE_MODE =
    Mode.inherit(Mode.TITLE_MODE, new Mode().begin(IDENT_RE));

  private static Mode PARAMS_MODE =
    new Mode().className("params").begin("\\([^\\(]").returnBegin().contains(new Mode[] {
      new Mode().begin("\\(").end("\\)").keywords(KEYWORDS).contains(ArrayJavaMethods.concat(EXPRESSIONS, new Mode[] {
        Mode.SELF,
      }))
    });
  private static Mode FUNCTION = new Mode()
    .className("function")
    .begin("^\\s*" + IDENT_RE + "\\s*=\\s*" + POSSIBLE_PARAMS_RE)
    .end("[-=]>")
    .returnBegin()
    .contains(new Mode[] {
      TITLE_MODE,
      PARAMS_MODE,
    });
  private static Mode FUNCTION_ANONYM =
    new Mode().begin("[:\\(,=]\\s*").relevance(0).contains(new Mode[] {
      new Mode().className("function").begin(POSSIBLE_PARAMS_RE).end("[-=]>").contains(new Mode[] {
        PARAMS_MODE,
      }).returnBegin()
    });
  private static Mode CLASS_DEFINITION = new Mode()
    .className("class")
    .beginKeywords(new Keyword[] { new Keyword("", "class") })
    .end("$")
    .illegal("[:=\"\\[\\]]")
    .contains(new Mode[] {
      TITLE_MODE,
      new Mode()
        .beginKeywords(new Keyword[] { new Keyword("", "extends") })
        .endsWithParent()
        .illegal("[:=\"\\[\\]]")
        .contains(new Mode[] { TITLE_MODE }),
    });
  private static Mode LABEL =
    new Mode().begin(IDENT_RE + ":").end(":").returnBegin().returnEnd().relevance(0);

  static {
    SUBST.contains(EXPRESSIONS);
  }

  public Language build() {
    return (Language) new Language()
      .aliases(ALIASES)
      .keywords(KEYWORDS)
      .contains(ArrayJavaMethods.concat(EXPRESSIONS, new Mode[] {
        Mode.HASH_COMMENT_MODE,
        Mode.COMMENT("###", "###", null),
        FUNCTION,
        FUNCTION_ANONYM,
        CLASS_DEFINITION,
        LABEL,
      }))
      .illegal("/\\*");
  }
}

class ECMAScript {
  static final String IDENT_RE = "[A-Za-z$_][0-9A-Za-z$_]*";
  static final String[] KEYWORDS = {
    "as", // for exports
    "in",
    "of",
    "if",
    "for",
    "while",
    "finally",
    "var",
    "new",
    "function",
    "do",
    "return",
    "void",
    "else",
    "break",
    "catch",
    "instanceof",
    "with",
    "throw",
    "case",
    "default",
    "try",
    "switch",
    "continue",
    "typeof",
    "delete",
    "let",
    "yield",
    "const",
    "class",
    // JS handles these with a special rule
    // "get",
    // "set",
    "debugger",
    "async",
    "await",
    "static",
    "import",
    "from",
    "export",
    "extends"
  };
  static final String[] LITERALS = {
    "true",
    "false",
    "null",
    "undefined",
    "NaN",
    "Infinity"
  };
  static final String[] TYPES = {
    // Fundamental objects
    "Object",
    "Function",
    "Boolean",
    "Symbol",
    // numbers and dates
    "Math",
    "Date",
    "Number",
    "BigInt",
    // text
    "String",
    "RegExp",
    // Indexed collections
    "Array",
    "Float32Array",
    "Float64Array",
    "Int8Array",
    "Uint8Array",
    "Uint8ClampedArray",
    "Int16Array",
    "Int32Array",
    "Uint16Array",
    "Uint32Array",
    "BigInt64Array",
    "BigUint64Array",
    // Keyed collections
    "Set",
    "Map",
    "WeakSet",
    "WeakMap",
    // Structured data
    "ArrayBuffer",
    "SharedArrayBuffer",
    "Atomics",
    "DataView",
    "JSON",
    // Control abstraction objects
    "Promise",
    "Generator",
    "GeneratorFunction",
    "AsyncFunction",
    // Reflection
    "Reflect",
    "Proxy",
    // Internationalization
    "Intl",
    // WebAssembly
    "WebAssembly"
  };
  static final String[] ERROR_TYPES = {
    "Error",
    "EvalError",
    "InternalError",
    "RangeError",
    "ReferenceError",
    "SyntaxError",
    "TypeError",
    "URIError"
  };
  static final String[] BUILT_IN_GLOBALS = {
    "setInterval",
    "setTimeout",
    "clearInterval",
    "clearTimeout",
  
    "require",
    "exports",
  
    "eval",
    "isFinite",
    "isNaN",
    "parseFloat",
    "parseInt",
    "decodeURI",
    "decodeURIComponent",
    "encodeURI",
    "encodeURIComponent",
    "escape",
    "unescape"
  };
  static final String[] BUILT_IN_VARIABLES = {
    "arguments",
    "this",
    "super",
    "console",
    "window",
    "document",
    "localStorage",
    "sessionStorage",
    "module",
    "global" // Node.js
  };
  static final String[] BUILT_INS =
    ArrayJavaMethods.concat(
      ArrayJavaMethods.concat(
        BUILT_IN_GLOBALS,
        TYPES
      ),
      ERROR_TYPES
    );
}

class ArrayJavaMethods {
  static <T> T[] concat(T[] left, T[] right) {
    T[] result = Arrays.copyOf(left, left.length + right.length);
    System.arraycopy(right, 0, result, left.length, right.length);
    return result;
  }

  @SuppressWarnings("unchecked")
  static <T> T[] exclude(T[] left, T[] right) {
    return Arrays
      .stream(left)
      .filter(elem -> ! Arrays.stream(right).anyMatch(elem::equals))
      .toArray(capacity -> (T[]) Array.newInstance(left.getClass().getComponentType(), capacity));
  }
}
