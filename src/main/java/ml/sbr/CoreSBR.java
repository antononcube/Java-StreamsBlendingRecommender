package main.java.ml.sbr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class CoreSBR extends AbstractSBR {

    // Logger
    public static final Logger logger = Logger.getLogger(CoreSBR.class.getName());

    //========================================================
    // Data members
    //========================================================
    Map<String, String> smrMatrix;
    Map<String, Map<String, Double>> itemInverseIndexes;
    Map<String, Map<String, Double>> tagInverseIndexes;

    Map<String, ArrayList<String>> tagTypeToTags;
    Map<String, Double> globalWeights;

    Set<String> knownTags;
    Set<String> knownItems;

    //========================================================
    // Getters and Setters
    //========================================================

    public Map<String, String> getSmrMatrix() {
        return smrMatrix;
    }

    public void setSmrMatrix(Map<String, String> smrMatrix) {
        this.smrMatrix = smrMatrix;
    }

    public Map<String, Map<String, Double>> getItemInverseIndexes() {
        if ( this.itemInverseIndexes == null || this.itemInverseIndexes.isEmpty() ) {
            this.transposeTagInverseIndexes();
        }
        return itemInverseIndexes;
    }

    public void setItemInverseIndexes(Map<String, Map<String, Double>> itemInverseIndexes) {
        this.itemInverseIndexes = itemInverseIndexes;
    }

    public Map<String, Map<String, Double>> getTagInverseIndexes() {
        return tagInverseIndexes;
    }

    public void setTagInverseIndexes(Map<String, Map<String, Double>> tagInverseIndexes) {
        this.tagInverseIndexes = tagInverseIndexes;
    }

    public Map<String, ArrayList<String>> getTagTypeToTags() {
        return tagTypeToTags;
    }

    public void setTagTypeToTags(Map<String, ArrayList<String>> tagTypeToTags) {
        this.tagTypeToTags = tagTypeToTags;
    }

    public Map<String, Double> getGlobalWeights() {
        return globalWeights;
    }

    public void setGlobalWeights(Map<String, Double> globalWeights) {
        this.globalWeights = globalWeights;
    }

    public Set<String> getKnownTags() {
        return knownTags;
    }

    public void setKnownTags(Set<String> knownTags) {
        this.knownTags = knownTags;
    }

    public Set<String> getKnownItems() {
        return knownItems;
    }

    public void setKnownItems(Set<String> knownItems) {
        this.knownItems = knownItems;
    }


    //========================================================
    // Clone
    //========================================================

    //========================================================
    // Read file into a list of strings
    //========================================================

    /**
     * Reads a file and adds its data to an ArrayList.
     *
     * @param filename File name with SMR matrix data.
     * @return ArrayList filled with data.
     */
    protected ArrayList<String> readFileToList(String filename) {
        if (filename == null || filename.trim().isEmpty()) return null;
        ArrayList<String> list = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            // Skip header line
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(commaSplit); // split on commas if not in double quotes
                String data = splitLine[1].replaceAll("\"", ""); // remove quotes
                list.add(data);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        logger.info("Finished reading file to list: " + filename);
        return list;
    }

    //========================================================
    // Ingest a SMR matrix three CSV files
    //========================================================

    /**
     * This method reads in SMR matrix triplet files and makes a hashmap of hashmaps.
     * I.e. tag inverse indexes.
     *
     * @param dataPrefix Prefix for the SMR matrix files.
     * @return smrMatrix Hashmap of (Input Tag to (Output Tag to Weight))
     *
     * Adapted from https://codereview.stackexchange.com/q/46465
     */
    protected Map<String, Map<String, Double>> ingestAsTagInverseIndexes(String dataPrefix) {

        if (dataPrefix == null || dataPrefix.trim().isEmpty()) {
            logger.warning("Empty data prefix argument.");
            return null;
        }

        Map<String, Map<String, Double>> tagInverseIndexes = new HashMap<>();

        // Read in row names and column names files and create the corresponding ArrayLists
        // >>> Existence of these files should be checked! <<<
        ArrayList<String> rownamesList = readFileToList(dataPrefix + "-rownames.csv");
        ArrayList<String> colnamesList = readFileToList(dataPrefix + "-colnames.csv");

        // Iterate through matrix file and construct the SMR matrix
        // using the row names and column names obtained above
        try (InputStream inpStream = getClass().getClassLoader().getResourceAsStream(dataPrefix + ".csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inpStream))) {

            String line = reader.readLine(); // skip past header

            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(commaSplit); // split on commas if not in double quotes
                // Get indices from matrix file
                int rownameIndex = new Integer(splitLine[0]) - 1; // -1 to account for 1-indexing
                int colnameIndex = new Integer(splitLine[1]) - 1; // -1 to account for 1-indexing
                Double weight = new Double(splitLine[2]);
                // Use indices to get item and tag from lists
                String item = rownamesList.get(rownameIndex);
                String tag = colnamesList.get(colnameIndex);

                if (tagInverseIndexes.containsKey(tag)) {
                    // If this profile metadata tag is already in the map, then add this item to it
                    tagInverseIndexes.get(tag).put(item, weight);
                } else {
                    // Create a new map for this profile metadata tag that contains this item only
                    Map<String, Double> itemToWeight = new HashMap<>();
                    itemToWeight.put(item, weight);
                    tagInverseIndexes.put(tag, itemToWeight);
                }
            }
            logger.info("Finished ingesting of the SMR matrix files with prefix: " + dataPrefix);
        } catch (Exception e) {
            logger.warning(e.getLocalizedMessage());
        }

        // Result
        return tagInverseIndexes;
    }

    //========================================================
    // Ingest a SMR matrix CSV file
    //========================================================

    public void ingestCSVMatrices(String dataPrefix) {

        // Start time
        final long then = System.nanoTime();

        // Ingest
        this.setTagInverseIndexes(ingestAsTagInverseIndexes(dataPrefix));
        this.setKnownTags(this.getTagInverseIndexes().keySet());

        // Log timing
        final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - then);
        logger.info(".transposeTagInverseIndexes for (ms): " + millis);
    }


    //========================================================
    // Make tag inverse indexes
    //========================================================

    // Created at ingestion -- see ingestAsTagInverseIndexes

    //========================================================
    // Transpose tag inverse indexes
    //========================================================

    public void transposeTagInverseIndexes() {

        // Start time
        final long then = System.nanoTime();

        // Transpose
        this.setItemInverseIndexes(this.transposeTagInverseIndexes(this.getTagInverseIndexes()));
        this.setKnownItems(this.getItemInverseIndexes().keySet());

        // Log timing
        final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - then);
        logger.info(".transposeTagInverseIndexes for (ms): " + millis);
    }

    //========================================================
    // Profile
    //========================================================

    /**
     * Compute profile for scored items.
     *
     * @param items     A string array of items.
     * @param normalize A Boolean: should the recommendations be normalized or not?
     * @param warn      A Boolean: should warning messages be given or not?
     * @return A list of string-double pairs sorted in descending order of their values.
     */
    public LinkedHashMap<String, Double> profile(
            Map<String, Double> items,
            Boolean normalize,
            Boolean warn,
            Boolean ignoreUnknown) {

        // Start time
        final long then = System.nanoTime();

        // Transpose inverse indexes if needed
        if (this.getItemInverseIndexes() == null || this.getItemInverseIndexes().isEmpty()) {
            this.transposeTagInverseIndexes();
        }

        // Make sure items are known

        // Compute the profile
        Map<String, Double> itemMix = new HashMap<String, Double>();

        Boolean foundOne = false;

        for (Map.Entry<String, Double> entry : items.entrySet()) {

            String item = entry.getKey();
            Double weight = entry.getValue();

            if (this.getItemInverseIndexes().containsKey(item)) {

                foundOne = true;
                this.mergeIntoStream(itemMix, this.getItemInverseIndexes().get(item), weight);

            } else if (!ignoreUnknown) {
                String msg = "The item " + item + " is unknown.";
                logger.warning(msg);
                if (warn) {
                    System.out.println(msg);
                }
                return new LinkedHashMap<>();
            }
        }

        // Check if at least one tag was found
        if (!foundOne) {
            String msg = "None of the items is known.";
            logger.warning(msg);
            if (warn) {
                System.out.println(msg);
            }
            return new LinkedHashMap<>();
        }

        // Normalize
        if (normalize) {
            this.maxNormalize(itemMix);
        }

        // Reverse sort
        LinkedHashMap<String, Double> res = new LinkedHashMap<>();

        itemMix.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> res.put(x.getKey(), x.getValue()));

        // Log timing
        final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - then);
        logger.info(".profile for (ms): " + millis);

        // Result
        return res;
    }

    //========================================================
    // Recommend by history
    //========================================================

    /**
     * Compute recommendations by array items.
     * Makes a profile and delegates to recommendByProfile.
     *
     * @param items     An string-double (hash-)map of scored items.
     * @param nrecs     A positive integer for the (maximum) number of recommendations.
     * @param normalize A Boolean: should the recommendations be normalized or not?
     * @param warn      A Boolean: should warning messages be given or not?
     * @return A list of string-double pairs sorted in descending order of their values.
     * @see recommendByProfile
     */
    public LinkedHashMap<String, Double> recommend(
            Map<String, Double> items,
            Integer nrecs,
            Boolean normalize,
            Boolean ignoreUnknown,
            Boolean warn) {

        // Start time
        final long then = System.nanoTime();

        // It is not fast, but it is just easy to compute the profile and call recommendByProfile.
        Map<String, Double> prof = this.profile(items, false, ignoreUnknown, warn);

        LinkedHashMap<String, Double> res = this.recommendByProfile(prof, nrecs, normalize, ignoreUnknown, warn);

        // Log timing
        final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - then);
        logger.info(".recommend for (ms): " + millis);

        return res;
    }

    //========================================================
    // Recommend by profile
    //========================================================

    /**
     * Compute recommendations by profile.
     *
     * @param profile   A (hash-)map that is a profile. The keys are tags, the values are scores.
     * @param nrecs     A positive integer for the (maximum) number of recommendations.
     * @param normalize A Boolean: should the recommendations be normalized or not?
     * @param warn      A Boolean: should warning messages be given or not?
     * @return A list of string-double pairs sorted in descending order of their values.
     */
    public LinkedHashMap<String, Double> recommendByProfile(
            ArrayList<String> profile,
            Integer nrecs,
            Boolean normalize,
            Boolean ignoreUnknown,
            Boolean warn) {

        Map<String, Double> profile2 = new HashMap<String, Double>();
        profile.forEach(tag -> profile2.put(tag, 1.0));

        return recommendByProfile(profile2, nrecs, normalize, ignoreUnknown, warn);
    }

    /**
     * Compute recommendations by profile.
     *
     * @param profile   A (hash-)map that is a profile. The keys are tags, the values are scores.
     * @param nrecs     A positive integer for the (maximum) number of recommendations.
     * @param normalize A Boolean: should the recommendations be normalized or not?
     * @param warn      A Boolean: should warning messages be given or not?
     * @return A list of string-double pairs sorted in descending order of their values.
     */
    public LinkedHashMap<String, Double> recommendByProfile(
            Map<String, Double> profile,
            Integer nrecs,
            Boolean normalize,
            Boolean ignoreUnknown,
            Boolean warn) {

        // Start time
        final long then = System.nanoTime();

        // Compute the profile recommendations
        Map<String, Double> profMix = new HashMap<String, Double>();

        Boolean foundOne = false;

        for (Map.Entry<String, Double> entry : profile.entrySet()) {

            String tag = entry.getKey();
            Double weight = entry.getValue();

            if (this.getTagInverseIndexes().containsKey(tag)) {

                foundOne = true;
                this.mergeIntoStream(profMix, this.tagInverseIndexes.get(tag), weight);

            } else if (!ignoreUnknown) {
                String msg = "The tag " + tag + " is unknown.";
                logger.warning(msg);
                if (warn) {
                    System.out.println(msg);
                }
                return new LinkedHashMap<>();
            }
        }

        // Check if at least one tag was found
        if (!foundOne) {
            String msg = "None of the tags is known.";
            logger.warning(msg);
            if (warn) {
                System.out.println(msg);
            }
            return new LinkedHashMap<>();
        }

        // Normalize
        if (normalize) {
            this.maxNormalize(profMix);
        }

        // Reverse sort and pick top nrecs elements
        LinkedHashMap<String, Double> res = new LinkedHashMap<>();

        profMix.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> res.put(x.getKey(), x.getValue()));

        // Slow I presume, but I cannot figure out how to take quickly the first n elements.
        LinkedHashMap<String, Double> res2 = new LinkedHashMap<>();
        int k = 0;
        for (Map.Entry<String, Double> entry : res.entrySet()) {
            if (k++ < nrecs) {
                res2.put(entry.getKey(), entry.getValue());
            }
        }

        // Log timing
        final long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - then);
        logger.info(".recommendByProfile for (ms): " + millis);
        
        // Result
        return res2;
    }

    //========================================================
    // Filter by profile
    //========================================================
    /**
     * Filter items by profile
     * @param prof: A profile specification used to filter with.
     * @param type: The type of filtering one of "union" or "intersection".
     * @param warn: Should warnings be issued or not?
     * @return An array items.
      */
    public ArrayList<String> filterByProfile( ArrayList<String> prof,
                                              String type,
                                              Boolean warn) {

        //Map<String, Double> profMix = new HashMap<>();
        Set<String> profMix = new HashSet<>();

        if (type.toLowerCase().equals("intersection")) {

            profMix = this.getItemInverseIndexes().keySet();

            for ( int i = 0; i < prof.size(); i++ ) {
                profMix.retainAll(this.getTagInverseIndexes().get(prof.get(i)).keySet());
            }

        } else if (type.toLowerCase().equals("union")) {

            for ( int i = 0; i < prof.size(); i++ ) {
                profMix.addAll(this.getTagInverseIndexes().get(i).keySet());
            }

        } else {
            if (warn) { System.out.println("The value of the type argument is expected to be one of \"intersection\" or \"union\"."); }
            return new ArrayList<String>();
        }

        return new ArrayList<String>(profMix);
    }

    //========================================================
    // Retrieve by query elements
    //========================================================
    /** Retrieve by query elements.
    * @param should: A profile specification used to recommend with.
    * @param must: A profile specification used to filter with. The items in the result must have the tags in the must argument.
    * @param mustNot: A profile specification used to filter with. The items in the result must not have the tags in the must not argument.
    * @param mustType: The type of filtering with the must tags; one of "union" or "intersection".
    * @param mustNotType: The type of filtering with the must not tags; one of "union" or "intersection".
    * @param warn: Should warnings be issued or not?
    * @return An array of dictionary elements (items) sorted in descending order.
    **/
    public ArrayList<String> retrieveByQueryElements(
            ArrayList<String> should,
            ArrayList<String> must,
            ArrayList<String> mustNot,
            String mustType,
            String mustNotType,
            Boolean warn
    ) {

        // Preliminary check
        if ( should.isEmpty() && must.isEmpty() && mustNot.isEmpty() ) {
            logger.warning("All specifications are empty.");
            if (warn) {
                System.out.println("All specifications are empty.");
            }
        }

        // Should
        Set<String> shouldItems = new HashSet<String>();
        LinkedHashMap<String, Double> profRecs;
        if ( !should.isEmpty() && !must.isEmpty() ) {
            should.addAll(must);
            profRecs = this.recommendByProfile( should,10, false, false, false);
            profRecs.entrySet().forEach(x -> shouldItems.add(x.getKey()));
        }

        Set<String> res = new HashSet<String>();
        res.addAll(shouldItems);

        // Must
        Set<String> mustItems = new HashSet<String>();
        if( !must.isEmpty() ) {
            Set<String> finalMustItems = mustItems;
            this.filterByProfile(must, mustType, warn).forEach(x -> { finalMustItems.add(x); });
        } else {
            mustItems = this.getTagInverseIndexes().keySet();
        }

        res.retainAll(mustItems);

        // Must Not
        Set<String> mustNotItems = new HashSet<String>();
        if( !must.isEmpty() ) {
            Set<String> finalMustNotItems = mustNotItems;
            this.filterByProfile(mustNot, mustNotType, warn).forEach(x -> { finalMustNotItems.add(x); });
        } else {
            mustNotItems = this.getTagInverseIndexes().keySet();
        }

        res.removeAll(mustNotItems);

        // Result
        return new ArrayList<String>(res);
    }

    //========================================================
    // Normalize per tag type
    //========================================================
}
