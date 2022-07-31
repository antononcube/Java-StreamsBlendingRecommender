package main.java.ml.sbr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SBRBasicUsage {
    public static void main(String[] args) {

        //System.out.println("Here is a world.");

        CoreSBR sbrObj = new CoreSBR();

        sbrObj.logger.setLevel(Level.WARNING);

        System.out.println("=========================================================");
        sbrObj.ingestCSVMatrices("WLExampleData-SMR-M01");

        sbrObj.transposeTagInverseIndexes();

        System.out.println("=========================================================");
        Map<String, Double> prof = new HashMap<>();
        prof.put("ApplicationArea:Chemistry", 1.0);

        LinkedHashMap<String, Double> profileRecs = sbrObj.recommendByProfile(prof, 3, true, false, true);

        System.out.println(profileRecs);

        System.out.println("=========================================================");
        Map<String, Double> hist = new HashMap<>();
        hist.put("Statistics-Cabbages", 1.0);

        LinkedHashMap<String, Double> histRecs = sbrObj.recommend(hist, 3, true, false, true);

        System.out.println(histRecs);

        System.out.println("=========================================================");
        ArrayList<String> should = new ArrayList<>();
        ArrayList<String> must = new ArrayList<>();
        ArrayList<String> mustNot = new ArrayList<>();

        should.add("ColumnHeading:Age");
        must.add("ColumnHeading:Gender");

        ArrayList<String> retrieved = sbrObj.retrieveByQueryElements(should, must, mustNot, "intersection", "union", true);

        System.out.println(retrieved);

    }
}
