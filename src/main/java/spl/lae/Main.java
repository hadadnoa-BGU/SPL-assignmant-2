package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      // TODO: main

        if(args.length != 3){
            System.err.println("Not enough args");
        }

        int numOfThreads = Integer.parseInt(args[0]);
        String inputPath = args[1];
        String outputPath = args[2];

        try{
            InputParser inputParser = new InputParser();
            ComputationNode root = inputParser.parse(inputPath);

            root.associativeNesting();

            LinearAlgebraEngine engine = new LinearAlgebraEngine(numOfThreads);
            ComputationNode result = engine.run(root);
            OutputWriter.write(result.getMatrix(), outputPath);


        }catch (Exception e) {
            try {
                OutputWriter.write(e.getMessage(), outputPath);
            } catch (IOException io) {
                // last-resort failure
                io.printStackTrace();
            }
        }
    }
}