/*
 * FIXME: LICENCE
 */
package amesmarket.filereaders;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * A lexer/parser that reads python style dictionaries.
 *
 * Written to support reading names properties from data file sections.
 * Has not been incorperated yet, but it's here if needed.
 * @author Sean L. Mooney
 *
 */
public class DictionaryReader {

    //TOKEN TYPES
    private static final int SENTINEL = 1;
    private static final int STRING = 2;
    private static final int EOF = -1;

    private Token next;
    private DictLexer lexer;

    private boolean readFailed = false;
    private boolean hasWarning = false;
    private final StringBuilder errorMsg;
    private final StringBuilder warningMsgs;

    /**
     * Reader is not reentrant.
     *
     * Client is responsible for closing the reader if needed.
     *
     * @param r
     */
    public DictionaryReader(Reader r) {
        lexer = new DictLexer(r);
        errorMsg = new StringBuilder();
        warningMsgs = new StringBuilder();
    }

    /**
     * Check {@link #hasWarning()} and {@link #readFailed()} to see if the
     * dictionary was read cleanly.
     *
     * @return
     */
    public Map<String, String> read() {
        HashMap<String, String> dict = new HashMap<String, String>();

        try {
            readDict(dict);
        } catch (BadDataFileFormatException e) {
            readFailed = true;
            errorMsg.append(e.getMessage());
        } catch (IOException e) {
            readFailed = true;
            errorMsg.append("I/O Error\n");

            //only append if we have a non-empty message.
            String msg = e.getMessage();
            if (msg != null && !"".equals(msg))
                errorMsg.append(e.getMessage());
        }

        return dict;
    }

    public boolean readFailed() {
        return readFailed;
    }

    public boolean hasWarning() {
        return hasWarning;
    }

    public String getErrorMessage() {
        return errorMsg.toString();
    }

    public String getWarningMessage() {
        return warningMsgs.toString();
    }

    private void move() throws IOException {
        next = lexer.scan();
    }

    /**
     *
     * @param l
     * @param dict out param. Will be modified by the reader.
     * @throws IOException
     * @throws BadDataFileFormatException
     */
    private void readDict(HashMap<String, String> dict) throws IOException,
            BadDataFileFormatException {

        move();
        //Start value
        matchSymbol('{');

        do {
            if (next.tag == STRING) {
                //key value pairs
                readKeyValueList(dict);
            } else if (next.tag == EOF) {
                error("Unexpected End Of File");
            } else {
                matchSymbol('}');
                return; // found the end of the dict.
            }
        } while (true);

    }

    private void readKeyValueList(HashMap<String, String> dict)
            throws IOException, BadDataFileFormatException {
        do {
            readKeyValue(dict);
            move();
            //loop around for the next
            if (',' == next.cVal) {
                move();
                matchTag(STRING);
            } else {
                break;
            }
        } while (true);
    }

    private void readKeyValue(HashMap<String, String> dict) throws IOException,
            BadDataFileFormatException {
        String key, value;

        //string
        matchTag(STRING);
        key = next.sVal;

        //colon
        move();
        matchSymbol(':');

        //string
        matchTag(STRING);
        value = next.sVal;

        dict.put(key, value);
    }

    private void error(String s) throws BadDataFileFormatException {
        readFailed = true;
        if (next != null) {
            throw new BadDataFileFormatException(next.line, next.column, s);
        } else {
            throw new BadDataFileFormatException(s);
        }
    }

    //FIXME: Anything to warn?
    private void warning(String s) {
        hasWarning = true;
        if (next != null) {
            warningMsgs.append("Line: ");
            warningMsgs.append(next.line);
            warningMsgs.append("Column: ");
            warningMsgs.append(next.column);
            warningMsgs.append(" ");
        }
        warningMsgs.append(s);
        warningMsgs.append('\n');
    }

    private void matchTag(int expected) throws BadDataFileFormatException {
        if (next.tag != expected) {
            error("Expected " + tagDesc(expected) + " found "
                    + tagDesc(next.tag));
        }
    }

    private void matchSymbol(int expected) throws BadDataFileFormatException,
            IOException {
        if (expected != next.cVal) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected '");
            sb.append(Character.toChars(expected));
            sb.append("' found '");
            sb.append(Character.toChars(next.cVal));
            sb.append("'.");
            error(sb.toString());
        } else {
            move();
        }
    }

    private String tagDesc(int tag) {
        switch (tag) {
        case STRING:
            return "String";
        case EOF:
            return "EOF";
        case SENTINEL:
            return "Literal";
        default:
            return "Unknown";
        }
    }

    /**
     * A simple lexer.
     *
     * Based on Dragon book lexer.
     *
     * @author Sean L. Mooney
     *
     */
    private class DictLexer {
        int line = 1;
        int col = 1;
        int peek = ' ';
        final Reader in;

        public DictLexer(Reader r) {
            if (r == null)
                throw new IllegalArgumentException();
            in = r;
        }

        public Token scan() throws IOException {
            while (true) { //TODO-XX: Better/less special case whitespace/eol detection.
                readch();
                if (peek == ' ' || peek == '\t')
                    continue;
                else if(isEOL()){
                    newline();
                } else if (peek == '\n') {
                    newline();
                } else {
                    break;
                }
            }

            if (isSentinel()) {
                return new Token( //EOF or SENTINEL type.
                        //Reader class defines EOF as -1.
                        (peek == -1) ? EOF : SENTINEL, peek, null, line, col);
            }

            StringBuilder sb = new StringBuilder();
            do {
                sb.append(Character.toChars(peek));
                readch();

                if (isSentinel()) {
                    push(); //backup if we hit a sentinel.
                    break;
                }
            } while (!isWhitespace());

            return new Token(STRING, 0, sb.toString(), line, col);
        }

        boolean isWhitespace() {
            //FIXME: EOL
            return peek == ' ' || peek == '\t' || peek == '\n';
        }

        boolean isSentinel() {
            switch (peek) {
            //Fall through for all of the literal
            //characters we expect to find in our language.
            case -1: //EOF
            case '{':
            case '}':
            case ':':
            case ',':
                return true;
            default: //not a char we char about. must be a string, continue
                return false;
            }
        }

        void push() throws IOException {
            in.reset();
        }

        /**
         * EOL is \n \r\n or \r.
         * @return
         * @throws IOException
         */
        boolean isEOL() throws IOException {
            if (peek == '\r') { // \r(\n)?
                readch();
                if (peek == '\n') { // \r\n
                    return true;
                } else {
                    push(); //put the char back, we only have a \r.
                    return true;
                }
            } else {
                return false;
            }
        }

        //TODO-X: More efficient to maintain a buffer here
        //read from the stream when at the end of the buffer
        //and maintain counter to which buffer entry we are
        //currently at for the next peak.
        void readch() throws IOException {
            in.mark(2);
            peek = in.read();
            if (peek != -1) //EOL, nothing to be done.
                col++;
        }

        void newline() {
            line++;
            col = 1;
        }
    }

    private class Token {
        final int tag;
        final int cVal;
        final String sVal;
        final int line;
        final int column;

        Token(int tag, int cVal, String sVal, int line, int column) {
            this.tag = tag;
            this.cVal = cVal;
            this.sVal = sVal;
            this.line = line;
            this.column = column;
        }
    }
}
