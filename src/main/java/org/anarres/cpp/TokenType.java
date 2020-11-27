/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.cpp;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.anarres.cpp.Token.AND_EQ;
import static org.anarres.cpp.Token.ARROW;
import static org.anarres.cpp.Token.CHARACTER;
import static org.anarres.cpp.Token.CPP_COMMENT;
import static org.anarres.cpp.Token.C_COMMENT;
import static org.anarres.cpp.Token.DEC;
import static org.anarres.cpp.Token.DIV_EQ;
import static org.anarres.cpp.Token.ELLIPSIS;
import static org.anarres.cpp.Token.EOF;
import static org.anarres.cpp.Token.EQ;
import static org.anarres.cpp.Token.GE;
import static org.anarres.cpp.Token.HASH;
import static org.anarres.cpp.Token.HEADER;
import static org.anarres.cpp.Token.IDENTIFIER;
import static org.anarres.cpp.Token.INC;
import static org.anarres.cpp.Token.INVALID;
import static org.anarres.cpp.Token.LAND;
import static org.anarres.cpp.Token.LAND_EQ;
import static org.anarres.cpp.Token.LE;
import static org.anarres.cpp.Token.LITERAL;
import static org.anarres.cpp.Token.LOR;
import static org.anarres.cpp.Token.LOR_EQ;
import static org.anarres.cpp.Token.LSH;
import static org.anarres.cpp.Token.LSH_EQ;
import static org.anarres.cpp.Token.MOD_EQ;
import static org.anarres.cpp.Token.MULT_EQ;
import static org.anarres.cpp.Token.M_ARG;
import static org.anarres.cpp.Token.M_PASTE;
import static org.anarres.cpp.Token.M_STRING;
import static org.anarres.cpp.Token.NE;
import static org.anarres.cpp.Token.NL;
import static org.anarres.cpp.Token.NUMBER;
import static org.anarres.cpp.Token.OR_EQ;
import static org.anarres.cpp.Token.PASTE;
import static org.anarres.cpp.Token.PLUS_EQ;
import static org.anarres.cpp.Token.P_LINE;
import static org.anarres.cpp.Token.RANGE;
import static org.anarres.cpp.Token.RSH;
import static org.anarres.cpp.Token.RSH_EQ;
import static org.anarres.cpp.Token.SQSTRING;
import static org.anarres.cpp.Token.STRING;
import static org.anarres.cpp.Token.SUB_EQ;
import static org.anarres.cpp.Token.WHITESPACE;
import static org.anarres.cpp.Token.XOR_EQ;

/**
 * @author shevek
 */
class TokenType {

	private static final List<TokenType> TYPES = new ArrayList<>();

	private static void addTokenType(int type, @Nonnull String name, @Nullable String text) {
		while (TYPES.size() <= type) TYPES.add(null);
		TYPES.set(type, new TokenType(name, text));
	}

	private static void addTokenType(int type, @Nonnull String name) {
		addTokenType(type, name, null);
	}

	@Nullable
	public static TokenType getTokenType(int type) {
		try {
			return TYPES.get(type);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Nonnull
	public static String getTokenName(int type) {
		TokenType tokenType = getTokenType(type);
		return tokenType != null ? tokenType.getName() : "Unknown" + type;
	}

	@Nullable
	public static String getTokenText(int type) {
		TokenType tokenType = getTokenType(type);
		return tokenType != null ? tokenType.getText() : null;
	}

	static {
		for (int i = 0; i < 255; i++) {
			String text = String.valueOf((char) i);
			addTokenType(i, text, text);
		}
		addTokenType(AND_EQ, "AND_EQ", "&=");
		addTokenType(ARROW, "ARROW", "->");
		addTokenType(CHARACTER, "CHARACTER");
		addTokenType(C_COMMENT, "CCOMMENT");
		addTokenType(CPP_COMMENT, "CPPCOMMENT");
		addTokenType(DEC, "DEC", "--");
		addTokenType(DIV_EQ, "DIV_EQ", "/=");
		addTokenType(ELLIPSIS, "ELLIPSIS", "...");
		addTokenType(EOF, "EOF");
		addTokenType(EQ, "EQ", "==");
		addTokenType(GE, "GE", ">=");
		addTokenType(HASH, "HASH", "#");
		addTokenType(HEADER, "HEADER");
		addTokenType(IDENTIFIER, "IDENTIFIER");
		addTokenType(INC, "INC", "++");
		addTokenType(NUMBER, "NUMBER");
		addTokenType(LAND, "LAND", "&&");
		addTokenType(LAND_EQ, "LAND_EQ", "&&=");
		addTokenType(LE, "LE", "<=");
		addTokenType(LITERAL, "LITERAL");
		addTokenType(LOR, "LOR", "||");
		addTokenType(LOR_EQ, "LOR_EQ", "||=");
		addTokenType(LSH, "LSH", "<<");
		addTokenType(LSH_EQ, "LSH_EQ", "<<=");
		addTokenType(MOD_EQ, "MOD_EQ", "%=");
		addTokenType(MULT_EQ, "MULT_EQ", "*=");
		addTokenType(NE, "NE", "!=");
		addTokenType(NL, "NL");
		addTokenType(OR_EQ, "OR_EQ", "|=");
		addTokenType(PASTE, "PASTE", "##");
		addTokenType(PLUS_EQ, "PLUS_EQ", "+=");
		addTokenType(RANGE, "RANGE", "..");
		addTokenType(RSH, "RSH", ">>");
		addTokenType(RSH_EQ, "RSH_EQ", ">>=");
		addTokenType(SQSTRING, "SQSTRING");
		addTokenType(STRING, "STRING");
		addTokenType(SUB_EQ, "SUB_EQ", "-=");
		addTokenType(WHITESPACE, "WHITESPACE");
		addTokenType(XOR_EQ, "XOR_EQ", "^=");
		addTokenType(M_ARG, "M_ARG");
		addTokenType(M_PASTE, "M_PASTE");
		addTokenType(M_STRING, "M_STRING");
		addTokenType(P_LINE, "P_LINE");
		addTokenType(INVALID, "INVALID");
	}

	private final String name;
	private final String text;

	TokenType(@Nonnull String name, @Nullable String text) {
		this.name = name;
		this.text = text;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nullable
	public String getText() {
		return text;
	}
}
