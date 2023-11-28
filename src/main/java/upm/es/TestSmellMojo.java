package upm.es;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import upm.es.testsmell.AbstractSmell;
import upm.es.testsmell.ResultsWriter;
import upm.es.testsmell.TestFile;
import upm.es.testsmell.TestSmellDetector;
import upm.es.thresholds.DefaultThresholds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;



@Mojo(name = "testSmells", defaultPhase = LifecyclePhase.COMPILE)
public class TestSmellMojo
    extends AbstractMojo
{
    
    //TODO "${pathToTestFiles}" por sustitución de variables --> INVESTIGAR MAVEN
    @Parameter(property = "pathToTestFiles")
    private String pathToTestFiles;

    public void execute()
        throws MojoExecutionException
    {
        
        System.out.println(pathToTestFiles);
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        if (pathToTestFiles == null) {
            System.out.println("null"+"Please provide the file containing the paths to the collection of test files"+pathToTestFiles);
            return;
        }
        if(!pathToTestFiles.isEmpty()){
            File inputFile = new File(pathToTestFiles);
            if(!inputFile.exists() || inputFile.isDirectory()) {
                System.out.println("isEmpty() "+"Please provide the file containing the paths to the collection of test files"+pathToTestFiles);
                return;
            }
        }

        if(pathToTestFiles.equals(new String(""))){
            System.out.println("equals "+"Please provide the file containing the paths to the collection of test files"+pathToTestFiles);
            return;
        }
        // TODO: DefaultThresholds estaba en el Kotlin, hace la estimacion de los testsmells, indica un rango

        TestSmellDetector testSmellDetector = new TestSmellDetector(new DefaultThresholds());

        /*
          Read the input file and build the TestFile objects
         */

        // try-with-resources cierra siempre un fichero sin usar un finally y eso
        try (BufferedReader in = new BufferedReader(new FileReader(pathToTestFiles))){
            
            String str;

            String[] lineItem;
            TestFile testFile;
            List<TestFile> testFiles = new ArrayList<>();
            while ((str = in.readLine()) != null) {
            // use comma as separator
            lineItem = str.split(",");

            //check if the test file has an associated production file
            if(lineItem.length ==2){
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            }
            else{
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }

            testFiles.add(testFile);
            }

            /*
            Initialize the output file - Create the output file and add the column names
            */
            ResultsWriter resultsWriter = ResultsWriter.createResultsWriter();
            List<String> columnNames;
            List<String> columnValues;

            columnNames = testSmellDetector.getTestSmellNames();
            columnNames.add(0, "App");
            columnNames.add(1, "TestClass");
            columnNames.add(2, "TestFilePath");
            columnNames.add(3, "ProductionFilePath");
            columnNames.add(4, "RelativeTestFilePath");
            columnNames.add(5, "RelativeProductionFilePath");
            resultsWriter.rowWritingLine(true);
            resultsWriter.writeColumnName(columnNames);

            /*
            Iterate through all test files to detect smells and then write the output
            */
            TestFile tempFile;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date;
            for (TestFile file : testFiles) {
                date = new Date();
                System.out.println(dateFormat.format(date) + " Processing: "+file.getTestFilePath());
                System.out.println("Processing: "+file.getTestFilePath());

                //detect smells
                tempFile = testSmellDetector.detectSmells(file);

                //write output
                columnValues = new ArrayList<>();
                columnValues.add(file.getApp());
                columnValues.add(file.getTestFileName());
                columnValues.add(file.getTestFilePath());
                columnValues.add(file.getProductionFilePath());
                columnValues.add(file.getRelativeTestFilePath());
                columnValues.add(file.getRelativeProductionFilePath());
                for (AbstractSmell smell : tempFile.getTestSmells()) {
                    try {
                        // TODO: getHasSmell() se pone getNumberOfSmellyTests()
                        columnValues.add(String.valueOf(smell.getNumberOfSmellyTests()));
                    }
                    catch (NullPointerException e){
                        columnValues.add("");
                    }
                }
            resultsWriter.writeLine(columnValues); 
            resultsWriter.rowWritingLine(false);
        }   
        System.out.println("end");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
