package zemberek.morphology.old_lexicon;

import static zemberek.core.turkish.PhoneticAttribute.FirstLetterConsonant;
import static zemberek.core.turkish.PhoneticAttribute.FirstLetterVowel;
import static zemberek.core.turkish.PhoneticAttribute.HasNoVowel;
import static zemberek.core.turkish.PhoneticAttribute.LastLetterConsonant;
import static zemberek.core.turkish.PhoneticAttribute.LastLetterVoiceless;
import static zemberek.core.turkish.PhoneticAttribute.LastLetterVowel;
import static zemberek.core.turkish.PhoneticAttribute.LastVowelBack;
import static zemberek.core.turkish.PhoneticAttribute.LastVowelFrontal;
import static zemberek.core.turkish.PhoneticAttribute.LastVowelRounded;
import static zemberek.core.turkish.PhoneticAttribute.LastVowelUnrounded;
import static zemberek.core._turkish._TurkishAlphabet.L_a;
import static zemberek.core._turkish._TurkishAlphabet.L_e;
import static zemberek.core._turkish._TurkishAlphabet.L_i;
import static zemberek.core._turkish._TurkishAlphabet.L_ii;
import static zemberek.core._turkish._TurkishAlphabet.L_u;
import static zemberek.core._turkish._TurkishAlphabet.L_uu;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import zemberek.core._turkish._TurkishAlphabet;
import zemberek.core.turkish.PhoneticAttribute;
import zemberek.core.turkish.PhoneticExpectation;
import zemberek.core.turkish.TurkicLetter;
import zemberek.core._turkish.TurkishLetterSequence;
import zemberek.morphology.old_lexicon.graph.SuffixData;
import zemberek.morphology.old_lexicon.graph.SuffixSurfaceNode;
import zemberek.morphology.old_lexicon.graph.TerminationType;
import zemberek.core.turkish.Turkish;

public class SuffixSurfaceNodeGenerator {

  public SuffixSurfaceNode getEmptyNode(
      EnumSet<PhoneticAttribute> attrs,
      EnumSet<PhoneticExpectation> expectations,
      SuffixData suffixData,
      SuffixForm set) {
    return generate(attrs, expectations, suffixData, set).get(0);
  }

  public List<SuffixSurfaceNode> generate(
      EnumSet<PhoneticAttribute> attrs,
      EnumSet<PhoneticExpectation> expectations,
      SuffixData suffixData,
      SuffixForm suffixForm) {

    List<SuffixToken> tokenList = Lists
        .newArrayList(new SuffixStringTokenizer(suffixForm.generation));

    // zero length token
    if (tokenList.size() == 0) {
      return Lists.newArrayList(
          new SuffixSurfaceNode(
              suffixForm,
              "",
              attrs.clone(),
              expectations.clone(),
              suffixData,
              suffixForm.terminationType));
    }

    List<SuffixSurfaceNode> forms = new ArrayList<SuffixSurfaceNode>(1);

    // generation of forms. normally only one form is generated. But in situations like cI~k, two Forms are generated.
    TurkishLetterSequence seq = new TurkishLetterSequence();
    int index = 0;
    for (SuffixToken token : tokenList) {
      EnumSet<PhoneticAttribute> formAttrs = defineMorphemicAttributes(seq, attrs);
      switch (token.type) {
        case LETTER:
          seq.append(token.letter);
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(suffixForm, seq.toString(),
                defineMorphemicAttributes(seq, attrs), suffixForm.terminationType));
          }
          break;

        case A_WOVEL:
          if (index == 0 && attrs.contains(LastLetterVowel)) {
            break;
          }
          TurkicLetter lA = TurkicLetter.UNDEFINED;
          if (formAttrs.contains(LastVowelBack)) {
            lA = L_a;
          } else if (formAttrs.contains(LastVowelFrontal)) {
            lA = L_e;
          }
          if (lA == TurkicLetter.UNDEFINED) {
            throw new IllegalArgumentException("Cannot generate A form!");
          }
          seq.append(lA);
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(
                suffixForm,
                seq.toString(),
                defineMorphemicAttributes(seq, attrs),
                suffixForm.terminationType));
          }
          break;

        case I_WOVEL:
          if (index == 0 && attrs.contains(LastLetterVowel)) {
            break;
          }
          TurkicLetter li = TurkicLetter.UNDEFINED;
          if (formAttrs.containsAll(Arrays.asList(LastVowelBack, LastVowelRounded))) {
            li = L_u;
          } else if (formAttrs.containsAll(Arrays.asList(LastVowelBack, LastVowelUnrounded))) {
            li = L_ii;
          } else if (formAttrs.containsAll(Arrays.asList(LastVowelFrontal, LastVowelRounded))) {
            li = L_uu;
          } else if (formAttrs.containsAll(Arrays.asList(LastVowelFrontal, LastVowelUnrounded))) {
            li = L_i;
          }
          if (li == TurkicLetter.UNDEFINED) {
            throw new IllegalArgumentException("Cannot generate I form!");
          }
          seq.append(li);
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(suffixForm, seq.toString(),
                defineMorphemicAttributes(seq, attrs), suffixForm.terminationType));
          }
          break;

        case APPEND:
          if (formAttrs.contains(LastLetterVowel)) {
            seq.append(token.letter);
          }
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(
                suffixForm,
                seq.toString(),
                defineMorphemicAttributes(seq, attrs),
                suffixForm.terminationType));
          }
          break;

        case DEVOICE_FIRST:
          TurkicLetter ld = token.letter;
          if (formAttrs.contains(LastLetterVoiceless)) {
            ld = _TurkishAlphabet.INSTANCE.devoice(token.letter);
          }
          seq.append(ld);
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(
                suffixForm,
                seq.toString(),
                defineMorphemicAttributes(seq, attrs),
                suffixForm.terminationType));
          }
          break;

        case VOICE_LAST:
          ld = token.letter;
          seq.append(ld);
          if (index == tokenList.size() - 1) {
            forms.add(new SuffixSurfaceNode(
                suffixForm,
                seq.toString(),
                defineMorphemicAttributes(seq, attrs),
                EnumSet.of(PhoneticExpectation.ConsonantStart),
                suffixData,
                suffixForm.terminationType));
            seq.changeLast(_TurkishAlphabet.INSTANCE.voice(token.letter));
            forms.add(new SuffixSurfaceNode(
                suffixForm,
                seq.toString(),
                defineMorphemicAttributes(seq, attrs),
                EnumSet.of(PhoneticExpectation.VowelStart),
                suffixData,
                TerminationType.NON_TERMINAL));
          }
          break;
      }
      index++;
    }
    return forms;
  }

  // in suffix, defining morphemic attributes is straight forward.
  EnumSet<PhoneticAttribute> defineMorphemicAttributes(TurkishLetterSequence seq,
      EnumSet<PhoneticAttribute> predecessorAttrs) {
    if (seq.length() == 0) {
      return EnumSet.copyOf(predecessorAttrs);
    }
    EnumSet<PhoneticAttribute> attrs = EnumSet.noneOf(PhoneticAttribute.class);
    if (seq.hasVowel()) {
      if (seq.lastVowel().isFrontal()) {
        attrs.add(LastVowelFrontal);
      } else {
        attrs.add(LastVowelBack);
      }
      if (seq.lastVowel().isRounded()) {
        attrs.add(LastVowelRounded);
      } else {
        attrs.add(LastVowelUnrounded);
      }
      if (seq.lastLetter().isVowel()) {
        attrs.add(LastLetterVowel);
      } else {
        attrs.add(LastLetterConsonant);
      }
      if (seq.firstLetter().isVowel()) {
        attrs.add(FirstLetterVowel);
      } else {
        attrs.add(FirstLetterConsonant);
      }
    } else {
      // we transfer vowel attributes from the predecessor attributes.
      attrs = EnumSet.copyOf(predecessorAttrs);
      attrs.addAll(Arrays.asList(LastLetterConsonant, FirstLetterConsonant, HasNoVowel));
      attrs.remove(LastLetterVowel);
    }
    if (seq.lastLetter().isVoiceless()) {
      attrs.add(PhoneticAttribute.LastLetterVoiceless);
      if (seq.lastLetter().isStopConsonant()) {
        // kitap
        attrs.add(PhoneticAttribute.LastLetterVoicelessStop);
      }
    } else {
      attrs.add(PhoneticAttribute.LastLetterVoiced);
    }
    return attrs;
  }

  EnumSet<PhoneticAttribute> defineMorphemicAttributes(TurkishLetterSequence seq) {
    return defineMorphemicAttributes(seq, EnumSet.noneOf(PhoneticAttribute.class));
  }

  private enum TokenType {
    I_WOVEL,
    A_WOVEL,
    DEVOICE_FIRST,
    VOICE_LAST,
    APPEND,
    LETTER
  }

  private static class SuffixToken {

    TokenType type;
    TurkicLetter letter;
    boolean append = false;

    private SuffixToken(TokenType type, TurkicLetter letter) {
      this.type = type;
      this.letter = letter;
    }

    private SuffixToken(TokenType type, TurkicLetter letter, boolean append) {
      this.type = type;
      this.letter = letter;
      this.append = append;
    }
  }

  class SuffixStringTokenizer implements Iterator<SuffixToken> {

    private final String generationWord;
    private int pointer;

    public SuffixStringTokenizer(String generationWord) {
      this.generationWord = generationWord;
    }

    public boolean hasNext() {
      return generationWord != null && pointer < generationWord.length();
    }

    public SuffixToken next() {
      if (!hasNext()) {
        throw new NoSuchElementException("no elements left!");
      }
      char c = generationWord.charAt(pointer++);
      char cNext = 0;
      if (pointer < generationWord.length()) {
        cNext = generationWord.charAt(pointer);
      }

      switch (c) {
        case '+':
          pointer++;
          if (cNext == 'I') {
            return new SuffixToken(TokenType.I_WOVEL, TurkicLetter.UNDEFINED, true);
          } else if (cNext == 'A') {
            return new SuffixToken(TokenType.A_WOVEL, TurkicLetter.UNDEFINED, true);
          } else {
            return new SuffixToken(TokenType.APPEND, Turkish.Alphabet.getLetter(cNext));
          }
        case '>':
          pointer++;
          return new SuffixToken(TokenType.DEVOICE_FIRST, Turkish.Alphabet.getLetter(cNext));
        case '~':
          pointer++;
          return new SuffixToken(TokenType.VOICE_LAST, Turkish.Alphabet.getLetter(cNext));
        case 'I':
          return new SuffixToken(TokenType.I_WOVEL, TurkicLetter.UNDEFINED);
        case 'A':
          return new SuffixToken(TokenType.A_WOVEL, TurkicLetter.UNDEFINED);
        default:
          return new SuffixToken(TokenType.LETTER, Turkish.Alphabet.getLetter(c));

      }
    }

    public void remove() {
    }
  }
}
