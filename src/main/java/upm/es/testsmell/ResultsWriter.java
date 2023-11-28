package upm.es.testsmell;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

/**
 * This class is utilized to write output to a CSV file
 */
public class ResultsWriter {

    private String outputFile;
    private FileWriter writer;

    /**
     * Creates the file into which output it to be written into. Results from each file will be stored in a new file
     * @throws IOException
     */
    private ResultsWriter() throws IOException {
        String time =  String.valueOf(Calendar.getInstance().getTimeInMillis());
        outputFile = MessageFormat.format("{0}_{1}_{2}.{3}", "Output","TestSmellDetection",time, "html");
        System.out.println(outputFile);
        writer = new FileWriter(outputFile,false);
    }

    /**
     * Factory method that provides a new instance of the ResultsWriter
     * @return new ResultsWriter instance
     * @throws IOException
     */
    public static ResultsWriter createResultsWriter() throws IOException {
        return new ResultsWriter();
    }

    public void rowWritingLine(boolean row) throws IOException {
        writer = new FileWriter(outputFile,true);
        if(row){
            writer.append("<style>table {  font-family: arial, sans-serif;  border-collapse: collapse;  width: 100%;}td, th {  border: 1px solid #dddddd;  text-align: left;  padding: 8px;}tr:nth-child(even) {  background-color: #dddddd;}</style>" + 
                         "<body><h2>Resultados</h2><table>"
                         );
        }else{
            writer.append("</table></body>");
        }
        writer.flush();
        writer.close();
    }

    /**
     * Writes column names into the CSV file
     * @param columnNames the column names
     * @throws IOException
     */
    public void writeColumnName(List<String> columnNames) throws IOException {
        writeOutput(columnNames);
    }

    /**
     * Writes column values into the CSV file
     * @param columnValues the column values
     * @throws IOException
     */
    public void writeLine(List<String> columnValues) throws IOException {
        writeOutput(columnValues);
    }

    /**
     * Appends the input values into the CSV file
     * @param dataValues the data that needs to be written into the file
     * @throws IOException
     */
    private void writeOutput(List<String> dataValues)throws IOException {
        writer = new FileWriter(outputFile,true);
        writer.append(" <tr>");
        for (int i=0; i<dataValues.size(); i++) {
            writer.append("<td>"+String.valueOf(dataValues.get(i))+"<td>");
            if(i==dataValues.size()-1){
                writer.append("</tr>");
                //writer.append(System.lineSeparator());
            }
        }
        writer.flush();
        writer.close();
    }
}
