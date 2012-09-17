package fr.openstreetmap.watch.matching.josmexpr;
// License: GPL. Copyright 2007 by Immanuel Scholz and others

import java.io.PushbackReader;
import java.io.StringReader;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


import fr.openstreetmap.watch.matching.josmexpr.PushbackTokenizer;
import fr.openstreetmap.watch.matching.josmexpr.PushbackTokenizer.Range;
import fr.openstreetmap.watch.matching.josmexpr.PushbackTokenizer.Token;
import fr.openstreetmap.watch.model.NodeDescriptor;
import fr.openstreetmap.watch.model.OsmPrimitive;
import fr.openstreetmap.watch.model.WayDescriptor;

/**
 Implements a google-like search.
 <br>
 Grammar:
<pre>
expression =
  fact | expression
  fact expression
  fact

fact =
 ( expression )
 -fact
 term?
 term=term
 term:term
 term
 </pre>

 @author Imi
 */
public class SearchCompiler {

    private boolean caseSensitive = false;
    private boolean regexSearch = false;
    private static String  rxErrorMsg ="The regex \"{0}\" had a parse error at offset {1}, full error:\n\n{2}";
    private static String  rxErrorMsgNoPos = "The regex \"{0}\" had a parse error, full error:\n\n{1}";
    private PushbackTokenizer tokenizer;
    private static Map<String, SimpleMatchFactory> simpleMatchFactoryMap = new HashMap<String, SimpleMatchFactory>();
    private static Map<String, UnaryMatchFactory> unaryMatchFactoryMap = new HashMap<String, UnaryMatchFactory>();
    private static Map<String, BinaryMatchFactory> binaryMatchFactoryMap = new HashMap<String, BinaryMatchFactory>();

    public SearchCompiler(boolean caseSensitive, boolean regexSearch, PushbackTokenizer tokenizer) {
        this.caseSensitive = caseSensitive;
        this.regexSearch = regexSearch;
        this.tokenizer = tokenizer;

        /* register core match factories at first instance, so plugins should
         * never be able to generate a NPE
         */
        if (simpleMatchFactoryMap.isEmpty()) {
            addMatchFactory(new CoreSimpleMatchFactory());
        }
        if (unaryMatchFactoryMap.isEmpty()) {
            addMatchFactory(new CoreUnaryMatchFactory());
        }

    }

    /**
     * Add (register) MatchFactory with SearchCompiler
     * @param factory
     */
    public static void addMatchFactory(MatchFactory factory) {
        for (String keyword : factory.getKeywords()) {
            // TODO: check for keyword collisions
            if (factory instanceof SimpleMatchFactory) {
                simpleMatchFactoryMap.put(keyword, (SimpleMatchFactory)factory);
            } else if (factory instanceof UnaryMatchFactory) {
                unaryMatchFactoryMap.put(keyword, (UnaryMatchFactory)factory);
            } else if (factory instanceof BinaryMatchFactory) {
                binaryMatchFactoryMap.put(keyword, (BinaryMatchFactory)factory);
            } else
                throw new AssertionError("Unknown match factory");
        }
    }

    public class CoreSimpleMatchFactory implements SimpleMatchFactory {
        private Collection<String> keywords = Arrays.asList("id", "version",
                "changeset", "nodes", "tags", "areasize", "modified", "selected",
                "incomplete", "untagged", "closed", "new", "indownloadarea",
                "allindownloadarea", "inview", "allinview", "timestamp");

        @Override
        public Match get(String keyword, PushbackTokenizer tokenizer) throws ParseError {
            //            if ("modified".equals(keyword))
            //                return new Modified();
            //            else if ("selected".equals(keyword))
            //                return new Selected();
            //            else if ("incomplete".equals(keyword))
            //                return new Incomplete();
            if ("untagged".equals(keyword))
                return new Untagged();
            else if ("closed".equals(keyword))
                return new Closed();
            //            else if ("new".equals(keyword))
            //                return new New();
            //            else if ("indownloadedarea".equals(keyword))
            //                return new InDataSourceArea(false);
            //            else if ("allindownloadedarea".equals(keyword))
            //                return new InDataSourceArea(true);
            //            else if ("inview".equals(keyword))
            //                return new InView(false);
            //            else if ("allinview".equals(keyword))
            //                return new InView(true);
            else if (tokenizer != null) {
                if ("id".equals(keyword))
                    return new Id(tokenizer);
                else if ("version".equals(keyword))
                    return new Version(tokenizer);
                else if ("changeset".equals(keyword))
                    return new ChangesetId(tokenizer);
                else if ("nodes".equals(keyword))
                    return new NodeCountRange(tokenizer);
                else if ("tags".equals(keyword))
                    return new TagCountRange(tokenizer);
                //                else if ("areasize".equals(keyword))
                //                    return new AreaSize(tokenizer);
                else if ("timestamp".equals(keyword)) {
                    String rangeS = " " + tokenizer.readTextOrNumber() + " "; // add leading/trailing space in order to get expected split (e.g. "a--" => {"a", ""})
                    String[] rangeA = rangeS.split("/");
                    if (rangeA.length == 1)
                        return new KeyValue(keyword, rangeS.trim(), regexSearch, caseSensitive);
                    else if (rangeA.length == 2) {
                        String rangeA1 = rangeA[0].trim();
                        String rangeA2 = rangeA[1].trim();
                        long minDate = DateUtils.fromString(rangeA1.isEmpty() ? "1980" : rangeA1).getTime(); // if min timestap is empty: use lowest possible date
                        long maxDate = rangeA2.isEmpty() ? new Date().getTime() : DateUtils.fromString(rangeA2).getTime(); // if max timestamp is empty: use "now"
                        return new TimestampRange(minDate, maxDate);
                    } else
                        /*
                         * I18n: Don't translate timestamp keyword
                         */ throw new ParseError(("Expecting <i>min</i>/<i>max</i> after ''timestamp''"));
                }
            }
            return null;
        }

        @Override
        public Collection<String> getKeywords() {
            return keywords;
        }
    }

    public static class CoreUnaryMatchFactory implements UnaryMatchFactory {
        private static Collection<String> keywords = Arrays.asList("parent", "child");

        @Override
        public UnaryMatch get(String keyword, Match matchOperand, PushbackTokenizer tokenizer) {
//            if ("parent".equals(keyword))
//                return new Parent(matchOperand);
//            else if ("child".equals(keyword))
//                return new Child(matchOperand);
            return null;
        }

        @Override
        public Collection<String> getKeywords() {
            return keywords;
        }
    }

    /**
     * Classes implementing this interface can provide Match operators.
     */
    private interface MatchFactory {
        public Collection<String> getKeywords();
    }

    public interface SimpleMatchFactory extends MatchFactory {
        public Match get(String keyword, PushbackTokenizer tokenizer) throws ParseError;
    }

    public interface UnaryMatchFactory extends MatchFactory {
        public UnaryMatch get(String keyword, Match matchOperand, PushbackTokenizer tokenizer) throws ParseError;
    }

    public interface BinaryMatchFactory extends MatchFactory {
        public BinaryMatch get(String keyword, Match lhs, Match rhs, PushbackTokenizer tokenizer) throws ParseError;
    }

    /**
     * Base class for all search operators.
     */
    abstract public static class Match {

        abstract public boolean match(OsmPrimitive osm);

        /**
         * Tests whether one of the primitives matches.
         */
        protected boolean existsMatch(Collection<? extends OsmPrimitive> primitives) {
            for (OsmPrimitive p : primitives) {
                if (match(p))
                    return true;
            }
            return false;
        }

        /**
         * Tests whether all primitives match.
         */
        protected boolean forallMatch(Collection<? extends OsmPrimitive> primitives) {
            for (OsmPrimitive p : primitives) {
                if (!match(p))
                    return false;
            }
            return true;
        }
    }

    /**
     * A unary search operator which may take data parameters.
     */
    abstract public static class UnaryMatch extends Match {

        protected final Match match;

        public UnaryMatch(Match match) {
            if (match == null) {
                // "operator" (null) should mean the same as "operator()"
                // (Always). I.e. match everything
                this.match = new Always();
            } else {
                this.match = match;
            }
        }

        public Match getOperand() {
            return match;
        }
    }

    /**
     * A binary search operator which may take data parameters.
     */
    abstract public static class BinaryMatch extends Match {

        protected final Match lhs;
        protected final Match rhs;

        public BinaryMatch(Match lhs, Match rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public Match getLhs() {
            return lhs;
        }

        public Match getRhs() {
            return rhs;
        }
    }

    /**
     * Matches every OsmPrimitive.
     */
    public static class Always extends Match {
        public static Always INSTANCE = new Always();
        @Override public boolean match(OsmPrimitive osm) {
            return true;
        }
    }

    /**
     * Never matches any OsmPrimitive.
     */
    public static class Never extends Match {
        @Override
        public boolean match(OsmPrimitive osm) {
            return false;
        }
    }

    /**
     * Inverts the match.
     */
    public static class Not extends UnaryMatch {
        public Not(Match match) {super(match);}
        @Override public boolean match(OsmPrimitive osm) {
            return !match.match(osm);
        }
        @Override public String toString() {return "!"+match;}
        public Match getMatch() {
            return match;
        }
    }

    /**
     * Matches if the value of the corresponding key is ''yes'', ''true'', ''1'' or ''on''.
     */
    private static class BooleanMatch extends Match {
        private final String key;
        private final boolean defaultValue;

        public BooleanMatch(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
        @Override
        public boolean match(OsmPrimitive osm) {
            Boolean ret = OsmUtils.getOsmBoolean(osm.get(key));
            if (ret == null)
                return defaultValue;
            else
                return ret;
        }
    }

    /**
     * Matches if both left and right expressions match.
     */
    public static class And extends BinaryMatch {
        public And(Match lhs, Match rhs) {super(lhs, rhs);}
        @Override public boolean match(OsmPrimitive osm) {
            return lhs.match(osm) && rhs.match(osm);
        }
        @Override public String toString() {
            return lhs + " && " + rhs;
        }
    }

    /**
     * Matches if the left OR the right expression match.
     */
    public static class Or extends BinaryMatch {
        public Or(Match lhs, Match rhs) {super(lhs, rhs);}
        @Override public boolean match(OsmPrimitive osm) {
            return lhs.match(osm) || rhs.match(osm);
        }
        @Override public String toString() {
            return lhs + " || " + rhs;
        }
    }

    /**
     * Matches if the left OR the right expression match, but not both.
     */
    public static class Xor extends BinaryMatch {
        public Xor(Match lhs, Match rhs) {super(lhs, rhs);}
        @Override public boolean match(OsmPrimitive osm) {
            return lhs.match(osm) ^ rhs.match(osm);
        }
        @Override public String toString() {
            return lhs + " ^ " + rhs;
        }
    }

    /**
     * Matches objects with the given object ID.
     */
    private static class Id extends Match {
        private long id;
        public Id(long id) {
            this.id = id;
        }
        public Id(PushbackTokenizer tokenizer) throws ParseError {
            this(tokenizer.readNumber(("Primitive id expected")));
        }
        @Override public boolean match(OsmPrimitive osm) {
            return osm.getId() == id;
        }
        @Override public String toString() {return "id="+id;}
    }

    /**
     * Matches objects with the given changeset ID.
     */
    private static class ChangesetId extends Match {
        private long changesetid;
        public ChangesetId(long changesetid) {this.changesetid = changesetid;}
        public ChangesetId(PushbackTokenizer tokenizer) throws ParseError {
            this(tokenizer.readNumber(("Changeset id expected")));
        }
        @Override public boolean match(OsmPrimitive osm) {
            return osm.getChangesetId() == changesetid;
        }
        @Override public String toString() {return "changeset="+changesetid;}
    }

    /**
     * Matches objects with the given version number.
     */
    private static class Version extends Match {
        private long version;
        public Version(long version) {this.version = version;}
        public Version(PushbackTokenizer tokenizer) throws ParseError {
            this(tokenizer.readNumber(("Version expected")));
        }
        @Override public boolean match(OsmPrimitive osm) {
            return osm.getVersion() == version;
        }
        @Override public String toString() {return "version="+version;}
    }

    /**
     * Matches objects with the given key-value pair.
     */
    private static class KeyValue extends Match {
        private final String key;
        private final Pattern keyPattern;
        private final String value;
        private final Pattern valuePattern;
        private final boolean caseSensitive;

        public KeyValue(String key, String value, boolean regexSearch, boolean caseSensitive) throws ParseError {
            this.caseSensitive = caseSensitive;
            if (regexSearch) {
                int searchFlags = regexFlags(caseSensitive);

                try {
                    this.keyPattern = Pattern.compile(key, searchFlags);
                } catch (PatternSyntaxException e) {
                    throw new ParseError(rxErrorMsg + " " +  e.getPattern() + " " + e.getIndex() + " " + e.getMessage());
                } catch (Exception e) {
                    throw new ParseError(rxErrorMsgNoPos + " " +  key + " "+ e.getMessage());
                }
                try {
                    this.valuePattern = Pattern.compile(value, searchFlags);
                } catch (PatternSyntaxException e) {
                    throw new ParseError(rxErrorMsg + " " +  e.getPattern() + " " +  e.getIndex() + " " + e.getMessage());
                } catch (Exception e) {
                    throw new ParseError(rxErrorMsgNoPos + " " + value +" " +  e.getMessage());
                }
                this.key = key;
                this.value = value;

            } else if (caseSensitive) {
                this.key = key;
                this.value = value;
                this.keyPattern = null;
                this.valuePattern = null;
            } else {
                this.key = key.toLowerCase();
                this.value = value;
                this.keyPattern = null;
                this.valuePattern = null;
            }
        }

        @Override public boolean match(OsmPrimitive osm) {

            if (keyPattern != null) {
                if (!osm.hasKeys())
                    return false;

                /* The string search will just get a key like
                 * 'highway' and look that up as osm.get(key). But
                 * since we're doing a regex match we'll have to loop
                 * over all the keys to see if they match our regex,
                 * and only then try to match against the value
                 */

                for (String k: osm.keySet()) {
                    String v = osm.get(k);

                    Matcher matcherKey = keyPattern.matcher(k);
                    boolean matchedKey = matcherKey.find();

                    if (matchedKey) {
                        Matcher matcherValue = valuePattern.matcher(v);
                        boolean matchedValue = matcherValue.find();

                        if (matchedValue)
                            return true;
                    }
                }
            } else {
                String mv = null;

                if (key.equals("timestamp")) {
                    mv = "" + osm.getTimestamp();
                } else {
                    mv = osm.get(key);
                }

                if (mv == null)
                    return false;

                String v1 = caseSensitive ? mv : mv.toLowerCase();
                String v2 = caseSensitive ? value : value.toLowerCase();

                v1 = Normalizer.normalize(v1, Normalizer.Form.NFC);
                v2 = Normalizer.normalize(v2, Normalizer.Form.NFC);
                return v1.indexOf(v2) != -1;
            }

            return false;
        }
        @Override public String toString() {return key+"="+value;}
    }

    /**
     * Matches objects with the exact given key-value pair.
     */
    public static class ExactKeyValue extends Match {

        private enum Mode {
            ANY, ANY_KEY, ANY_VALUE, EXACT, NONE, MISSING_KEY,
            ANY_KEY_REGEXP, ANY_VALUE_REGEXP, EXACT_REGEXP, MISSING_KEY_REGEXP;
        }

        private final String key;
        private final String value;
        private final Pattern keyPattern;
        private final Pattern valuePattern;
        private final Mode mode;

        public ExactKeyValue(boolean regexp, String key, String value) throws ParseError {
            if ("".equals(key))
                throw new ParseError(("Key cannot be empty when tag operator is used. Sample use: key=value"));
            this.key = key;
            this.value = value == null?"":value;
            if ("".equals(this.value) && "*".equals(key)) {
                mode = Mode.NONE;
            } else if ("".equals(this.value)) {
                if (regexp) {
                    mode = Mode.MISSING_KEY_REGEXP;
                } else {
                    mode = Mode.MISSING_KEY;
                }
            } else if ("*".equals(key) && "*".equals(this.value)) {
                mode = Mode.ANY;
            } else if ("*".equals(key)) {
                if (regexp) {
                    mode = Mode.ANY_KEY_REGEXP;
                } else {
                    mode = Mode.ANY_KEY;
                }
            } else if ("*".equals(this.value)) {
                if (regexp) {
                    mode = Mode.ANY_VALUE_REGEXP;
                } else {
                    mode = Mode.ANY_VALUE;
                }
            } else {
                if (regexp) {
                    mode = Mode.EXACT_REGEXP;
                } else {
                    mode = Mode.EXACT;
                }
            }

            if (regexp && key.length() > 0 && !key.equals("*")) {
                try {
                    keyPattern = Pattern.compile(key, regexFlags(false));
                } catch (PatternSyntaxException e) {
                    throw new ParseError(rxErrorMsg + " " +  e.getPattern() + " " + e.getIndex() + " " + e.getMessage());
                } catch (Exception e) {
                    throw new ParseError(rxErrorMsgNoPos + " " +  key + " " + e.getMessage());
                }
            } else {
                keyPattern = null;
            }
            if (regexp && this.value.length() > 0 && !this.value.equals("*")) {
                try {
                    valuePattern = Pattern.compile(this.value, regexFlags(false));
                } catch (PatternSyntaxException e) {
                    throw new ParseError(rxErrorMsg + " " +  e.getPattern() + " " + e.getIndex() + " " + e.getMessage());
                } catch (Exception e) {
                    throw new ParseError(rxErrorMsgNoPos + " " +  key + " " + e.getMessage());
                }
            } else {
                valuePattern = null;
            }
        }

        @Override
        public boolean match(OsmPrimitive osm) {

            if (!osm.hasKeys())
                return mode == Mode.NONE;

            switch (mode) {
            case NONE:
                return false;
            case MISSING_KEY:
                return osm.get(key) == null;
            case ANY:
                return true;
            case ANY_VALUE:
                return osm.get(key) != null;
            case ANY_KEY:
                for (String v:osm.getKeys().values()) {
                    if (v.equals(value))
                        return true;
                }
                return false;
            case EXACT:
                return value.equals(osm.get(key));
            case ANY_KEY_REGEXP:
                for (String v:osm.getKeys().values()) {
                    if (valuePattern.matcher(v).matches())
                        return true;
                }
                return false;
            case ANY_VALUE_REGEXP:
            case EXACT_REGEXP:
                for (String key: osm.keySet()) {
                    if (keyPattern.matcher(key).matches()) {
                        if (mode == Mode.ANY_VALUE_REGEXP
                                || valuePattern.matcher(osm.get(key)).matches())
                            return true;
                    }
                }
                return false;
            case MISSING_KEY_REGEXP:
                for (String k:osm.keySet()) {
                    if (keyPattern.matcher(k).matches())
                        return false;
                }
                return true;
            }
            throw new AssertionError("Missed state");
        }

        @Override
        public String toString() {
            return key + '=' + value;
        }

    }

    /**
     * Match a string in any tags (key or value), with optional regex and case insensitivity.
     */
    private static class Any extends Match {
        private final String search;
        private final Pattern searchRegex;
        private final boolean caseSensitive;

        public Any(String s, boolean regexSearch, boolean caseSensitive) throws ParseError {
            s = Normalizer.normalize(s, Normalizer.Form.NFC);
            this.caseSensitive = caseSensitive;
            if (regexSearch) {
                try {
                    this.searchRegex = Pattern.compile(s, regexFlags(caseSensitive));
                } catch (PatternSyntaxException e) {
                    throw new ParseError(rxErrorMsg +  e.getPattern() +  e.getIndex()+  e.getMessage());
                } catch (Exception e) {
                    throw new ParseError(rxErrorMsgNoPos +  s +  e.getMessage());
                }
                this.search = s;
            } else if (caseSensitive) {
                this.search = s;
                this.searchRegex = null;
            } else {
                this.search = s.toLowerCase();
                this.searchRegex = null;
            }
        }

        @Override public boolean match(OsmPrimitive osm) {
            if (!osm.hasKeys() && osm.getUser() == null)
                return search.equals("");

            for (String key: osm.keySet()) {
                String value = osm.get(key);
                if (searchRegex != null) {

                    value = Normalizer.normalize(value, Normalizer.Form.NFC);

                    Matcher keyMatcher = searchRegex.matcher(key);
                    Matcher valMatcher = searchRegex.matcher(value);

                    boolean keyMatchFound = keyMatcher.find();
                    boolean valMatchFound = valMatcher.find();

                    if (keyMatchFound || valMatchFound)
                        return true;
                } else {
                    if (!caseSensitive) {
                        key = key.toLowerCase();
                        value = value.toLowerCase();
                    }

                    value = Normalizer.normalize(value, Normalizer.Form.NFC);

                    if (key.indexOf(search) != -1 || value.indexOf(search) != -1)
                        return true;
                }
            }
            return false;
        }
        @Override public String toString() {
            return search;
        }
    }

    // TODO: change how we handle this
    private static class ExactType extends Match {
        private final Class<?> type;
        public ExactType(String type) throws ParseError {
            if ("node".equals(type)) {
                this.type = NodeDescriptor.class;
            } else if ("way".equals(type)) {
                this.type = WayDescriptor.class;
                //            } else if ("relation".equals(type)) {
                //                this.type = Relation.class;
            } else
                throw new ParseError("Unknown primitive type" + type + " Allowed values are node, way or relation");
        }
        @Override public boolean match(OsmPrimitive osm) {
            return osm.getClass() == type;
        }
        @Override public String toString() {return "type="+type;}
    }

    /**
     * Matches objects last changed by the given username.
     */
    private static class UserMatch extends Match {
        private String user;
        public UserMatch(String user) {
            if (user.equals("anonymous")) {
                this.user = null;
            } else {
                this.user = user;
            }
        }

        @Override public boolean match(OsmPrimitive osm) {
            if (osm.getUser() == null)
                return user == null;
            else
                return osm.getUser().equals(user);
        }

        @Override public String toString() {
            return "user=" + user == null ? "" : user;
        }
    }

    /**
     * Matches objects with the given relation role (i.e. "outer").
     */
    private static class RoleMatch extends Match {
        private String role;
        public RoleMatch(String role) {
            if (role == null) {
                this.role = "";
            } else {
                this.role = role;
            }
        }

        @Override public boolean match(OsmPrimitive osm) {
            return false;//TODO
//            for (OsmPrimitive ref: osm.getReferrers()) {
//                if (ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted()) {
//                    for (RelationMember m : ((Relation) ref).getMembers()) {
//                        if (m.getMember() == osm) {
//                            String testRole = m.getRole();
//                            if(role.equals(testRole == null ? "" : testRole))
//                                return true;
//                        }
//                    }
//                }
//            }
//            return false;
        }

        @Override public String toString() {
            return "role=" + role;
        }
    }

    /**
     * Matches objects with properties in a certain range.
     */
    private abstract static class CountRange extends Match {

        private long minCount;
        private long maxCount;

        public CountRange(long minCount, long maxCount) {
            this.minCount = Math.min(minCount, maxCount);
            this.maxCount = Math.max(minCount, maxCount);
        }

        public CountRange(Range range) {
            this(range.getStart(), range.getEnd());
        }

        protected abstract Long getCount(OsmPrimitive osm);

        protected abstract String getCountString();

        @Override
        public boolean match(OsmPrimitive osm) {
            Long count = getCount(osm);
            if (count == null)
                return false;
            else
                return (count >= minCount) && (count <= maxCount);
        }

        @Override
        public String toString() {
            return getCountString() + "=" + minCount + "-" + maxCount;
        }
    }


    /**
     * Matches ways with a number of nodes in given range
     */
    private static class NodeCountRange extends CountRange {
        public NodeCountRange(Range range) {
            super(range);
        }

        public NodeCountRange(PushbackTokenizer tokenizer) throws ParseError {
            this(tokenizer.readRange(("Range of numbers expected")));
        }

        @Override
        protected Long getCount(OsmPrimitive osm) {
            if (!(osm instanceof WayDescriptor))
                return null;
            else
                return (long) ((WayDescriptor) osm).nodes.size();
        }

        @Override
        protected String getCountString() {
            return "nodes";
        }
    }

    /**
     * Matches objects with a number of tags in given range
     */
    private static class TagCountRange extends CountRange {
        public TagCountRange(Range range) {
            super(range);
        }

        public TagCountRange(PushbackTokenizer tokenizer) throws ParseError {
            this(tokenizer.readRange(("Range of numbers expected")));
        }

        @Override
        protected Long getCount(OsmPrimitive osm) {
            return (long) osm.getKeys().size();
        }

        @Override
        protected String getCountString() {
            return "tags";
        }
    }

    //    /**
    //     * Matches objects with a timestamp in given range
    //     */
    private static class TimestampRange extends CountRange {

        public TimestampRange(long minCount, long maxCount) {
            super(minCount, maxCount);
        }

        @Override
        protected Long getCount(OsmPrimitive osm) {
            return new Date(osm.getTimestamp()).getTime();
        }

        @Override
        protected String getCountString() {
            return "timestamp";
        }

    }
    //
    //    /**
    //     * Matches objects that are new (i.e. have not been uploaded to the server)
    //     */
    //    private static class New extends Match {
    //        @Override public boolean match(OsmPrimitive osm) {
    //            return osm.isNew();
    //        }
    //        @Override public String toString() {
    //            return "new";
    //        }
    //    }

    //    /**
    //     * Matches all objects that have been modified, created, or undeleted
    //     */
    //    private static class Modified extends Match {
    //        @Override public boolean match(OsmPrimitive osm) {
    //            return osm.isModified() || osm.isNewOrUndeleted();
    //        }
    //        @Override public String toString() {return "modified";}
    //    }

    /**
     * Matches all objects currently selected
     */
    //    private static class Selected extends Match {
    //        @Override public boolean match(OsmPrimitive osm) {
    //            return Main.main.getCurrentDataSet().isSelected(osm);
    //        }
    //        @Override public String toString() {return "selected";}
    //    }

    /**
     * Match objects that are incomplete, where only id and type are known.
     * Typically some members of a relation are incomplete until they are
     * fetched from the server.
     */
    //    private static class Incomplete extends Match {
    //        @Override public boolean match(OsmPrimitive osm) {
    //            return osm.isIncomplete();
    //        }
    //        @Override public String toString() {return "incomplete";}
    //    }

    /**
     * Matches objects that don't have any interesting tags (i.e. only has source,
     * FIXME, etc.). The complete list of uninteresting tags can be found here:
     * org.openstreetmap.josm.data.osm.OsmPrimitive.getUninterestingKeys()
     */
    private static class Untagged extends Match {
        @Override public boolean match(OsmPrimitive osm) {
            // FIXME return !osm.isTagged() && !osm.isIncomplete();
            return false;
        }
        @Override public String toString() {return "untagged";}
    }

    /**
     * Matches ways which are closed (i.e. first and last node are the same)
     */
    private static class Closed extends Match {
        @Override public boolean match(OsmPrimitive osm) {
            return osm instanceof WayDescriptor && ((WayDescriptor) osm).isClosed();
        }
        @Override public String toString() {return "closed";}
    }

    /**
     * Matches objects if they are parents of the expression
     */
    //    public static class Parent extends UnaryMatch {
    //        public Parent(Match m) {
    //            super(m);
    //        }
    //        @Override public boolean match(OsmPrimitive osm) {
    //            boolean isParent = false;
    //
    //            if (osm instanceof WayDescriptor) {
    //                for (Node n : ((Way)osm).getNodes()) {
    //                    isParent |= match.match(n);
    //                }
    //            } else if (osm instanceof Relation) {
    //                for (RelationMember member : ((Relation)osm).getMembers()) {
    //                    isParent |= match.match(member.getMember());
    //                }
    //            }
    //            return isParent;
    //        }
    //        @Override public String toString() {return "parent(" + match + ")";}
    //    }

    /**
     * Matches objects if they are children of the expression
     */
    //    public static class Child extends UnaryMatch {
    //
    //        public Child(Match m) {
    //            super(m);
    //        }
    //
    //        @Override public boolean match(OsmPrimitive osm) {
    //            boolean isChild = false;
    //            for (OsmPrimitive p : osm.getReferrers()) {
    //                isChild |= match.match(p);
    //            }
    //            return isChild;
    //        }
    //        @Override public String toString() {return "child(" + match + ")";}
    //    }

    /**
     * Matches if the size of the area is within the given range
     *
     * @author Ole Jørgen Brønner
     */
    //    private static class AreaSize extends CountRange {
    //
    //        public AreaSize(Range range) {
    //            super(range);
    //        }
    //
    //        public AreaSize(PushbackTokenizer tokenizer) throws ParseError {
    //            this(tokenizer.readRange(("Range of numbers expected")));
    //        }
    //
    //        @Override
    //        protected Long getCount(OsmPrimitive osm) {
    //            if (!(osm instanceof WayDescriptor && ((Way) osm).isClosed()))
    //                return null;
    //            Way way = (Way) osm;
    //            return (long) Geometry.closedWayArea(way);
    //        }
    //
    //        @Override
    //        protected String getCountString() {
    //            return "areasize";
    //        }
    //    }

    //    /**
    //     * Matches objects within the given bounds.
    //     */
    //    private abstract static class InArea extends Match {
    //
    //        protected abstract Bounds getBounds();
    //        protected final boolean all;
    //        protected final Bounds bounds;
    //
    //        /**
    //         * @param all if true, all way nodes or relation members have to be within source area;if false, one suffices.
    //         */
    //        public InArea(boolean all) {
    //            this.all = all;
    //            this.bounds = getBounds();
    //        }
    //
    //        @Override
    //        public boolean match(OsmPrimitive osm) {
    //            if (!osm.isUsable())
    //                return false;
    //            else if (osm instanceof Node)
    //                return bounds.contains(((Node) osm).getCoor());
    //            else if (osm instanceof Way) {
    //                Collection<Node> nodes = ((Way) osm).getNodes();
    //                return all ? forallMatch(nodes) : existsMatch(nodes);
    //            } else if (osm instanceof Relation) {
    //                Collection<OsmPrimitive> primitives = ((Relation) osm).getMemberPrimitives();
    //                return all ? forallMatch(primitives) : existsMatch(primitives);
    //            } else
    //                return false;
    //        }
    //    }
    //
    //    /**
    //     * Matches objects within source area ("downloaded area").
    //     */
    //    private static class InDataSourceArea extends InArea {
    //
    //        public InDataSourceArea(boolean all) {
    //            super(all);
    //        }
    //
    //        @Override
    //        protected Bounds getBounds() {
    //            return new Bounds(Main.main.getCurrentDataSet().getDataSourceArea().getBounds2D());
    //        }
    //    }
    //
    //    /**
    //     * Matches objects within current map view.
    //     */
    //    private static class InView extends InArea {
    //
    //        public InView(boolean all) {
    //            super(all);
    //        }
    //
    //        @Override
    //        protected Bounds getBounds() {
    //            return Main.map.mapView.getRealBounds();
    //        }
    //    }

    public static class ParseError extends Exception {
        public ParseError(String msg) {
            super(msg);
        }
        public ParseError(Token expected, Token found) {
            this("Unexpected token. Expected " + expected +"  found " + found);
        }
    }

    public static Match compile(String searchStr, boolean caseSensitive, boolean regexSearch)
            throws ParseError {
        return new SearchCompiler(caseSensitive, regexSearch,
                new PushbackTokenizer(
                        new PushbackReader(new StringReader(searchStr))))
        .parse();
    }

    /**
     * Parse search string.
     *
     * @return match determined by search string
     * @throws fr.openstreetmap.watch.matching.josmexpr.openstreetmap.josm.actions.search.SearchCompiler.ParseError
     */
    public Match parse() throws ParseError {
        Match m = parseExpression();
        if (!tokenizer.readIfEqual(Token.EOF))
            throw new ParseError(("Unexpected token: " +  tokenizer.nextToken()));
        if (m == null)
            return new Always();
        return m;
    }

    /**
     * Parse expression. This is a recursive method.
     *
     * @return match determined by parsing expression
     * @throws fr.openstreetmap.watch.matching.josmexpr.openstreetmap.josm.actions.search.SearchCompiler.ParseError
     */
    private Match parseExpression() throws ParseError {
        Match factor = parseFactor();
        if (factor == null)
            // empty search string
            return null;
        if (tokenizer.readIfEqual(Token.OR))
            return new Or(factor, parseExpression(("Missing parameter for OR")));
        else if (tokenizer.readIfEqual(Token.XOR))
            return new Xor(factor, parseExpression(("Missing parameter for XOR")));
        else {
            Match expression = parseExpression();
            if (expression == null)
                // reached end of search string, no more recursive calls
                return factor;
            else
                // the default operator is AND
                return new And(factor, expression);
        }
    }

    /**
     * Parse expression, showing the specified error message if parsing fails.
     *
     * @param errorMessage to display if parsing error occurs
     * @return
     * @throws fr.openstreetmap.watch.matching.josmexpr.openstreetmap.josm.actions.search.SearchCompiler.ParseError
     */
    private Match parseExpression(String errorMessage) throws ParseError {
        Match expression = parseExpression();
        if (expression == null)
            throw new ParseError(errorMessage);
        else
            return expression;
    }

    /**
     * Parse next factor (a search operator or search term).
     *
     * @return match determined by parsing factor string
     * @throws fr.openstreetmap.watch.matching.josmexpr.openstreetmap.josm.actions.search.SearchCompiler.ParseError
     */
    private Match parseFactor() throws ParseError {
        if (tokenizer.readIfEqual(Token.LEFT_PARENT)) {
            Match expression = parseExpression();
            if (!tokenizer.readIfEqual(Token.RIGHT_PARENT))
                throw new ParseError(Token.RIGHT_PARENT, tokenizer.nextToken());
            return expression;
        } else if (tokenizer.readIfEqual(Token.NOT)) {
            return new Not(parseFactor(("Missing operator for NOT")));
        } else if (tokenizer.readIfEqual(Token.KEY)) {
            // factor consists of key:value or key=value
            String key = tokenizer.getText();
            if (tokenizer.readIfEqual(Token.EQUALS))
                return new ExactKeyValue(regexSearch, key, tokenizer.readTextOrNumber());
            else if (tokenizer.readIfEqual(Token.COLON)) {
                // see if we have a Match that takes a data parameter
                SimpleMatchFactory factory = simpleMatchFactoryMap.get(key);
                if (factory != null)
                    return factory.get(key, tokenizer);

                UnaryMatchFactory unaryFactory = unaryMatchFactoryMap.get(key);
                if (unaryFactory != null)
                    return unaryFactory.get(key, parseFactor(), tokenizer);

                // key:value form where value is a string (may be OSM key search)
                return parseKV(key, tokenizer.readTextOrNumber());
            } else if (tokenizer.readIfEqual(Token.QUESTION_MARK))
                return new BooleanMatch(key, false);
            else {
                SimpleMatchFactory factory = simpleMatchFactoryMap.get(key);
                if (factory != null)
                    return factory.get(key, null);

                UnaryMatchFactory unaryFactory = unaryMatchFactoryMap.get(key);
                if (unaryFactory != null)
                    return unaryFactory.get(key, parseFactor(), null);

                // match string in any key or value
                return new Any(key, regexSearch, caseSensitive);
            }
        } else
            return null;
    }

    private Match parseFactor(String errorMessage) throws ParseError {
        Match fact = parseFactor();
        if (fact == null)
            throw new ParseError(errorMessage);
        else
            return fact;
    }

    private Match parseKV(String key, String value) throws ParseError {
        if (value == null) {
            value = "";
        }
        if (key.equals("type"))
            return new ExactType(value);
        else if (key.equals("user"))
            return new UserMatch(value);
        else if (key.equals("role"))
            return new RoleMatch(value);
        else
            return new KeyValue(key, value, regexSearch, caseSensitive);
    }

    private static int regexFlags(boolean caseSensitive) {
        int searchFlags = 0;

        // Enables canonical Unicode equivalence so that e.g. the two
        // forms of "\u00e9gal" and "e\u0301gal" will match.
        //
        // It makes sense to match no matter how the character
        // happened to be constructed.
        searchFlags |= Pattern.CANON_EQ;

        // Make "." match any character including newline (/s in Perl)
        searchFlags |= Pattern.DOTALL;

        // CASE_INSENSITIVE by itself only matches US-ASCII case
        // insensitively, but the OSM data is in Unicode. With
        // UNICODE_CASE casefolding is made Unicode-aware.
        if (!caseSensitive) {
            searchFlags |= (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }

        return searchFlags;
    }
}