import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import org.apache.poi.ss.usermodel.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 2/23/15
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class mapExcel2RDF {
    protected static  String SOURCE_FILE = "sample.owl";
    OntModel sample_Model = null;
    String dcPrefix="dcterms";
    String myprefix="myprefix";
    Individual resource_individual=null;
    String NS="http://www.semanticweb.org/ontologies/2015/1/Ontology1424698842747.owl#";
    String OutputFormat="TURTLE";



    protected void loadModel() {

        sample_Model=ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );
        sample_Model.setNsPrefix(dcPrefix,"http://purl.org/dc/terms/");
        sample_Model.setNsPrefix(myprefix,"http://www.semanticweb.org/ontologies/2015/1/Ontology1424698842747.owl#");
        Model baseOntology = FileManager.get().loadModel( SOURCE_FILE );
        sample_Model.addSubModel(baseOntology);


    }

    void processExcelFile(String inputExcelFilePath,int defaultSheet, String OWLPrefix)  {

        int counter=0, termID=0, termRelatedID=0, parentID=0;
        String subTerm=null;
        String subCat=null;
        String WorkSheetName=null;
        InputStream inp = null;
        FileOutputStream dumpFile;
        String cellValue=null;


        try {
            inp = new FileInputStream(inputExcelFilePath);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);
            Header header = sheet.getHeader();
            loadModel();
            OntClass LearningObject=sample_Model.getOntClass( "sampleObject" );
            resource_individual =sample_Model.createIndividual( NS, LearningObject );

            int rowsCount = sheet.getLastRowNum();
            System.out.println("Total Number of Rows: " + (rowsCount + 1));
            for (int i = 0; i <= rowsCount; i++) {
                Row row = sheet.getRow(i);
                int colCounts = row.getLastCellNum();
                System.out.println("Total Number of Cols: " + colCounts);
                for (int j = 0; j < colCounts; j++) {
                    Cell cell = row.getCell(j);
                    cellValue=cell.getStringCellValue();
                    System.out.println("[" + i + "," + j + "]=" + cellValue);
                    OntProperty title = sample_Model.getAnnotationProperty(DC.title.getURI());
                    System.out.println(title.getURI());
                    resource_individual.addProperty(title, cellValue);
                }
            }
            String dumpFileAddress="output.ttl";
            dumpFile=new FileOutputStream(dumpFileAddress);
            sample_Model.write(dumpFile, "TURTLE");

        } catch (Exception ex) {
            System.out.println("Error Number 1");
        } finally {
            try {
                inp.close();
            } catch (IOException ex) {
                System.out.println("Error Number 2");
            }
        }
        // rootID=terminology.findRoot(conn,root,baseSchema);


    }

    void processCSVFile(String inputCSVFilePath,int defaultSheet, String OWLPrefix)  {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        FileOutputStream dumpFile;

        try {
            loadModel();
            OntClass LearningObject=sample_Model.getOntClass( "sampleObject" );
            resource_individual =sample_Model.createIndividual( NS+"1", LearningObject );

            br = new BufferedReader(new FileReader(inputCSVFilePath));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] value = line.split(cvsSplitBy);

                //OntProperty title = sample_Model.getAnnotationProperty(DC.title.getURI());
                OntProperty size = sample_Model.getDatatypeProperty(NS+"size");
                //System.out.println(size.getURI());
                for(int i=0;i<value.length;i++)
                   resource_individual.addProperty(size, value[i]);

            }

            String dumpFileAddress="output.ttl";
            dumpFile=new FileOutputStream(dumpFileAddress);
            sample_Model.write(dumpFile, OutputFormat);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main( String[] args) {

         mapExcel2RDF obj=new mapExcel2RDF();
         //obj.processExcelFile("test.xlsx",1,"http://dcterms.org");
         obj.processCSVFile("test.csv",1,"http://dcterms.org");
    }

}
