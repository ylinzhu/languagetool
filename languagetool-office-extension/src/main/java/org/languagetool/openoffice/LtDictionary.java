/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.openoffice;

import java.util.ArrayList;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.DictionaryType;
import com.sun.star.linguistic2.XDictionary;
import com.sun.star.linguistic2.XDictionaryEntry;
import com.sun.star.linguistic2.XSearchableDictionaryList;
import com.sun.star.uno.XComponentContext;

/**
 * Class to add manual LT dictionaries temporarily to LibreOffice/OpenOffice
 * @since 5.0
 * @author Fred Kruse
 */
public class LtDictionary {
  
  private static boolean debugMode; //  should be false except for testing

  private List<String> dictinaryList = new ArrayList<String>();
  
  public LtDictionary() {
    debugMode = OfficeTools.DEBUG_MODE_LD;
  }

  public void setLtDictionary(XComponentContext xContext, Locale locale, LinguisticServices linguServices) {
    XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
    if(searchableDictionaryList == null) {
      MessageHandler.printToLogFile("searchableDictionaryList == null");
      return;
    }
    String shortCode = locale.Language;
    String dictionaryName = "__LT_" + shortCode + "_internal.dic";
    if(!dictinaryList.contains(dictionaryName)) {
      XDictionary manualDictionary = searchableDictionaryList.createDictionary(dictionaryName, locale, DictionaryType.POSITIVE, "");
      for (String word : getManualWordList(locale, linguServices)) {
        manualDictionary.add(word, false, "");
      }
      manualDictionary.setActive(true);
      searchableDictionaryList.addDictionary(manualDictionary);
      dictinaryList.add(dictionaryName);
      MessageHandler.printToLogFile("Internal LT dicitionary for language " + shortCode + " added: Number of words = " + manualDictionary.getCount());
      if (debugMode) {
        for (XDictionaryEntry entry : manualDictionary.getEntries()) {
          MessageHandler.printToLogFile(entry.getDictionaryWord());
        }
      }
    }
  }
  
  private List<String> getManualWordList(Locale locale, LinguisticServices linguServices) {
    List<String> words = new ArrayList<String>();
    String shortLangCode = locale.Language;
    String path = "/" + shortLangCode + "/added.txt";
    if(JLanguageTool.getDataBroker().resourceExists(path)) {
      List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
      if(lines != null) {
        for(String line : lines) {
          if(!line.isEmpty() && !line.startsWith("#")) {
            String[] lineWords = line.trim().split("\\h");
            if (!words.contains(lineWords[0]) && !linguServices.isCorrectSpell(lineWords[0], locale)) {
              words.add(lineWords[0]);
            }
          }
        }
      }
    }
    path = "/" + shortLangCode + "/spelling.txt";
    if(JLanguageTool.getDataBroker().resourceExists(path)) {
      List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
      if(lines != null) {
        for(String line : lines) {
          line = line.trim();
          if(!line.isEmpty() && !line.startsWith("#") && !linguServices.isCorrectSpell(line, locale)) {
            MessageHandler.printToLogFile(line);
          }
        }
      }
    }
    return words;
  }

}
