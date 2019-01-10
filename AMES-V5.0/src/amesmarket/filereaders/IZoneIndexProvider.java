/*
 * FIXME: Licence
 */
package amesmarket.filereaders;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Define a way to map zone names to array indices.
 *
 * @author Sean L. Mooney
 *
 */
public interface IZoneIndexProvider {
    /**
     * Get the index mapped to the name or -1 if nothing is known about the name.
     * @param name
     * @return
     */
    public int get(String name);
    /**
     * Check to see if the name is mapped.
     * @param name
     * @return
     */
    public boolean hasIndexForName(String name);

    public int getNumZones();

    /**
     * An index provider that can go from a string of form prefix[1-9][0-9]*
     * and strip off the prefix to get the zone index.
     * @author sean
     *
     */
    public class DefaultIndexProvider implements IZoneIndexProvider {

        private final Pattern pattern = Pattern.compile("^\\D*([1-9]\\d*)$");
        private final boolean zeroIndexed;

        /**
         * Create a non-zero indexed (since that is usually what we need)
         * DefaultIndexProvider.
         */
        public DefaultIndexProvider() {
            this(false);
        }

        /**
         *
         * @param zeroIndexed whether or not the zone names are zero indexed.
         * If true, will return N-1 for a string that looks like 'NameN'. Otherwise,
         * return N.
         */
        public DefaultIndexProvider(boolean zeroIndexed) {
            this.zeroIndexed = zeroIndexed;
        }

        @Override
        public int get(String name) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                int zn = Integer.parseInt(m.group(1));
                if (zeroIndexed)
                    return zn - 1;
                else
                    return zn;
            } else {
                return -1;
            }
        }

        @Override
        public boolean hasIndexForName(String name) {
            return pattern.matcher(name).find();
        }

        @Override
        public int getNumZones() {
            return Integer.MAX_VALUE;
        }

    }

    /**
     * A zone index provider that allows clients to set which name maps to
     * which index.
     * Backed by a {@link java.util.HashMap}.
     * @author Sean L. Mooney
     *
     */
    public class NamedIndexProvider implements IZoneIndexProvider {

        private final Map<String, Integer> map = new HashMap<String, Integer>();

        @Override
        public int get(String name) {
            Integer i = map.get(name);

            if(i != null){
                return i.intValue();
            } else {
                return -1;
            }
        }

        @Override
        public boolean hasIndexForName(String name) {
            return map.containsKey(name);
        }

        @Override
        public int getNumZones() {
            return map.size();
        }

        /**
         * Map the name to the index.
         * @param name
         * @param index
         */
        public void put(String name, int index) {
            map.put(name, index);
        }
    }

    /**
     * A {@link IZoneIndexProvider} that automatically creates a new index
     * for any name it does not know about. Useful for stand reads of
     * load case files. e.g. Unit tests and the LoadCaseVerifier.
     *
     * It isn't super smart about index collision. If you use this
     * class and manually add names with indexes, it may be the case
     * that the next automatically created index will clash with and
     * overwrite teh manually set option.
     *
     * @author Sean L. Mooney
     *
     */
    public class AutomaticIndexProvider extends NamedIndexProvider {
        //Zones numbering always starts at 1.
        private int nextIdx = 1;

        /**
         * Always has an index for the name.
         * Create the index if this is a name we don't know about.
         */
        @Override
        public boolean hasIndexForName(String name){
            if(!super.hasIndexForName(name)){
                put(name, nextIdx++);
            }

            return true;
        }

        /**
         * Always has an index for the name.
         * Create the index if this is a name we don't know about.
         */
        @Override
        public int get(String name) {
            if(super.hasIndexForName(name)) {
                return super.get(name);
            } else {
                int old = nextIdx;
                put(name, nextIdx++);
                return old;
            }
        }
    }
}
