package com.codewaves.codehighlight.core;

import com.codewaves.codehighlight.languages.*;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by Sergej Kravcenko on 5/17/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

/**
 * Main class for code syntax highlighting. Contains all supported languages
 * and two main methods. Use {@link Highlighter#highlight(String, String)} if
 * code language is known or use {@link Highlighter#highlightAuto(String, String[])}
 * to automatically detect code language.
 */
public class Highlighter<E> {
   private static final Map<String, Language> mLanguageMap;
   private static final String[] mLanguages;
   static {
      final Map<String, Language> languages = new HashMap<>();
      registerLanguage("apache", languages, new ApacheLanguage().build());
      registerLanguage("bash", languages, new BashLanguage().build());
      registerLanguage("coffeescript", languages, new CoffeescriptLanguage().build());
      registerLanguage("cpp", languages, new CppLanguage().build());
      registerLanguage("cs", languages, new CsLanguage().build());
      registerLanguage("css", languages, new CssLanguage().build());
      registerLanguage("diff", languages, new DiffLanguage().build());
      registerLanguage("groovy", languages, new GroovyLanguage().build());
      registerLanguage("go", languages, new GoLanguage().build());
      registerLanguage("http", languages, new HttpLanguage().build());
      registerLanguage("ini", languages, new IniLanguage().build());
      registerLanguage("java", languages, new JavaLanguage().build());
      registerLanguage("javascript", languages, new JsLanguage().build());
      registerLanguage("json", languages, new JsonLanguage().build());
      registerLanguage("makefile", languages, new MakefileLanguage().build());
      registerLanguage("markdown", languages, new MarkdownLanguage().build());
      registerLanguage("objectivec", languages, new ObjCLanguage().build());
      registerLanguage("perl", languages, new PerlLanguage().build());
      registerLanguage("php", languages, new PhpLanguage().build());
      registerLanguage("python", languages, new PythonLanguage().build());
      registerLanguage("ruby", languages, new RubyLanguage().build());
      registerLanguage("scala", languages, new ScalaLanguage().build());
      registerLanguage("shell", languages, new ShellLanguage().build());
      registerLanguage("sql", languages, new SqlLanguage().build());
      registerLanguage("xml", languages, new XmlLanguage().build());
      registerLanguage("yaml", languages, new YamlLanguage().build());
      mLanguageMap = languages;
      mLanguages = new String[] { "apache", "bash", "coffeescript", "cpp", "cs", "css", "diff", "go", "groovy", "http", "ini", "java",
              "javascript", "json", "makefile", "markdown", "objectivec", "perl", "php", "python", "ruby", "scala",
              "shell", "sql", "xml", "yaml" };
   }

   private static void registerLanguage(String name, Map<String, Language> languages, Language language) {
      languages.put(name, language);
      if (language.aliases != null) {
         for (String alias : language.aliases) {
            languages.put(alias, language);
         }
      }
   }

   static Language findLanguage(String name) {
      return mLanguageMap.get(name);
   }

   /**
    * Result of code syntax highlighting
    */
   public static class HighlightResult<R> {
      private int relevance;
      private String language;
      private R result;

      HighlightResult(int relevance, String language, R result) {
         this.relevance = relevance;
         this.language = language;
         this.result = result;
      }

      /**
       *
       * @return relevance
       */
      public int getRelevance() {
         return relevance;
      }

      /**
       *
       * @return detected language name
       */
      public String getLanguage() {
         return language;
      }

      /**
       *
       * @return highlighted code
       */
      public R getResult() {
         return result;
      }
   }

   private StyleRendererFactory<E> mRendererFactory;


   /**
    *
    * @param factory style renderer factory
    */
   public Highlighter(StyleRendererFactory<E> factory) {
      mRendererFactory = factory;
   }

   /**
    * Core highlighting function. Accepts a language name, or an alias, and a
    * string with the code to highlight.
    *
    * @param languageName language name
    * @param code code string to highlight
    *
    * @return the given code highlight result
    */
   public HighlightResult<E> highlight(String languageName, String code) {
       final StyleRenderer<E> renderer = mRendererFactory.create(languageName);
      // Find language by name
      final Language language = mLanguageMap.get(languageName);
      if (language == null) {
         renderer.onPushOriginalSubLanguage(null, code);
         return new HighlightResult<E>(0, null, renderer.getResult());
      }

      // Parse
      final HighlightParser<E> parser = new HighlightParser<>(language, mRendererFactory, renderer);
      final int relevance = parser.highlight(code, false, null);
      return new HighlightResult<E>(relevance, languageName, renderer.getResult());
   }

   /**
    * Highlighting with language detection. Accepts a string with the code to
    * highlight.
    *
    * @param code code string to highlight
    * @param languageSubset list of languages for checking or null for all known languages
    *
    * @return the given code highlight result
    */
   public HighlightResult<E> highlightAuto(String code, String[] languageSubset) {
      final String[] languages = (languageSubset == null || languageSubset.length == 0) ? mLanguages : languageSubset;

      int bestRelevance = 0;
      String bestLanguageName = null;
      E result = null;
      for (String languageName : languages) {
         final Language language = mLanguageMap.get(languageName);
         if (language == null) {
            continue;
         }

         final StyleRenderer<E> renderer = mRendererFactory.create(languageName);
         final HighlightParser<E> parser = new HighlightParser<>(language, mRendererFactory, renderer);
         final int relevance = parser.highlight(code, false, null);
         if (relevance > bestRelevance) {
            bestRelevance = relevance;
            bestLanguageName = languageName;
            result = renderer.getResult();
         }
      }

      return new HighlightResult<E>(bestRelevance, bestLanguageName, result);
   }
}
