package main.java.ml.sbr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSBR {

    // Regex for splitting on commas if not in double quotes
    // See https://stackoverflow.com/a/1757107
    protected static final String commaSplit = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    //========================================================
    // Norm computation
    //========================================================
    /**
     * Norm of a map.
     * @see norm
     */
    public static Double norm(Map<String, Double> mix) {
        return norm(mix.values(), "euclidean");
    }

    public static Double norm(Map<String, Double> mix, String spec ) {
        return norm(mix.values(), spec);
    }

    /**
     * Norm of a vector
     *
     * @param vec A collection of doubles to be normalized.
     * @param spec Norm type identifier, one of "euclidean", "max-norm", "one-norm".
     */
    public static Double norm(Collection<Double> vec) {
        return norm(vec, "euclidean");
    }

    private static Double norm(Collection<Double> values, String spec) {
        if ( spec.equals("euclidean") ) {
            return 0.0;
        } else if ( spec.equals("max-norm") ) {
            return 0.0;
        }
        return 0.0;
    }

    //========================================================
    // Specific normalizing functions
    //========================================================
    /**
     * Apply max-norm normalization to a stream / mix. (In place.)
     *
     * @param mix Hashmap to compute the norm of.
     */
    protected static void maxNormalize(Map<String, Double> mix) {
        if (mix == null || mix.isEmpty()) {
            // Probably some message should be given or logger info
            return;
        }

        double max = 0.0;

        // Iterate through the stream in order to get the max value
        for (Map.Entry<String, Double> entry : mix.entrySet()) {
            double t = Math.abs(entry.getValue());
            if (t > max) { max = t; }
        }

        // Divide each value by the max-norm
        if (max != 0.0) {
            for (Map.Entry<String, Double> entry : mix.entrySet()) {
                if (entry.getValue() != 0.0) entry.setValue(entry.getValue() / max);
            }
        }
    }

    /**
     *  Apply Cosine/Euclidean normalization to a stream / mix. (In place.)
     *
     * @param mix Hashmap to compute the norm of.
     */
    protected static void cosineNormalize(Map<String, Double> mix) {

        if (mix == null || mix.isEmpty()) {
            // Probably some message should be given or logger info
            return;
        }

        Double norm = 0.0;

        // Iterate through the stream in order to get the squared values sum
        for (Map.Entry<String, Double> entry : mix.entrySet()) {
            norm += Math.pow(entry.getValue(), 2);
        }

        // Compute the 2-norm
        norm = Math.sqrt(norm);

        // Divide each value by the 2-norm
        if (norm != 0.0) {
            for (Map.Entry<String, Double> entry : mix.entrySet()) {
                entry.setValue(entry.getValue() / norm);
            }
        }
    }

    //========================================================
    // Inverse index merging functions
    //========================================================
    /**
     *  Merge two streams into one stream using a given weight.
     *
     * @param stream Stream to merge into.
     * @param streamToAdd Stream to add.
     * @param weight Weight of the stream to add.
     */
    protected static void mergeIntoStream(Map<String, Double> stream, Map<String, Double> streamToAdd, Double weight) {

        if (stream != null && streamToAdd != null && !streamToAdd.isEmpty()) {
            for (Map.Entry<String, Double> entry : streamToAdd.entrySet()) {

                String tag = entry.getKey();

                if (stream.containsKey(tag)) {
                    stream.put(tag, Double.sum(stream.get(tag), weight * entry.getValue()));
                }
                else {
                    stream.put(tag, weight * entry.getValue());
                }
            }
        }
    }


    //========================================================
    // Transpose tag inverse indexes
    //========================================================

    /**
     * Transposes the given matrix represented as a hashmap of hashmaps.
     * For example, converts the tag-to-item hashmap of item-to-weight hashmaps into
     * an item-to-tag hashmap of tag-to-weight hashmaps.
     * This is equivalent to a sparse matrix transposition.
     * Also, relates to exchanging the format from column major to row major.
     *
     * @param inverseIndexes
     * @return
     */
    protected static Map<String, Map<String, Double>> transposeTagInverseIndexes(Map<String, Map<String, Double>> inverseIndexes) {
        // Create empty hashmap for the result
        Map<String, Map<String, Double>> res = new HashMap<>();

        // Iterate through input hashmap of hashmaps
        for (Map.Entry<String, Map<String, Double>> tagToItem : inverseIndexes.entrySet()) {

            String tag = tagToItem.getKey();

            Map<String, Double> itemToWeight = tagToItem.getValue();

            for (Map.Entry<String, Double> itemToWeightEntry : itemToWeight.entrySet()) {
                String item = itemToWeightEntry.getKey();
                Double weight = itemToWeightEntry.getValue();

                if (res.containsKey(item)) {
                    // If item is present

                    Map<String, Double> tagToWeight = res.get(item);

                    if (tagToWeight.containsKey(tag)) {
                        tagToWeight.put(tag, Double.sum(tagToWeight.get(tag), weight));
                    } else {
                        tagToWeight.put(tag, weight);
                    }

                } else {
                    // If item is not present

                    Map<String, Double> tagToWeight = new HashMap<>();

                    tagToWeight.put(tag, weight);
                    res.put(item, tagToWeight);
                }
            }
        }

        // Result
        return res;
    }
}
