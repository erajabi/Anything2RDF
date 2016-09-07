import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: erajabi
 * Date: 10/7/15
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class datasetEnrichment {
    public void addInterlinkingResults(Model sourceModel, Model LinkedModel){
        String dumpFileAddress="mymodel2.ttl";
        String outputFileFormat="TURTLE";

        StmtIterator iter = sourceModel.listStatements();
       /*while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object

            System.out.println(object.toString());


         }
          System.exit(1);*/
        iter = LinkedModel.listStatements();
        String linkedRelationship="http://www.w3.org/2002/07/owl#sameAs";
        Property sameAs = sourceModel.createProperty(linkedRelationship);
        int counter=0;
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            //RDFNode   object    = stmt.getObject();      // get the object
            String   object=stmt.getObject().toString().replaceAll("/resource", "");
            RDFNode objResource=sourceModel.createResource(object);
            //sourceModel.add(subject,sameAs,object.toString());
            sourceModel.add(objResource.asResource(),sameAs,subject.getURI());
            counter++;
        }
        System.out.println("Added link="+counter);
        FileOutputStream dumpFile=null;
        try{
            dumpFile=new FileOutputStream(dumpFileAddress);
        }catch(IOException e){
            System.out.println(e);
        }
        sourceModel.write(dumpFile,outputFileFormat);

    }
    public static void main(String args[]) {
        
        String linkedRelationship="http://www.w3.org/2002/07/owl#sameAs";
        String inputFilePath="learnweb_dump.ttl";
        String interlinkingResultFilePath="learnWeb_DBpedia_accepted_2015.nt";
        String dumpFileAddress="mymodel.ttl";
        String outputFileFormat="TURTLE";

        // Create a model and read into it from file
// "data.ttl" assumed to be Turtle.
        // create an empty model
        //Model model_out = ModelFactory.createDefaultModel();
        Model model_out = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);;

        // use the FileManager to find the input file
        InputStream in = FileManager.get().open( "test.ttl" );
       /* if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + inputFileName + " not found");
        }  */
        Model sourceModel = RDFDataMgr.loadModel(inputFilePath) ;
        Model interlinkingResultModel = RDFDataMgr.loadModel(interlinkingResultFilePath) ;
        
        datasetEnrichment datasetEnrichmentObj=new datasetEnrichment();
            datasetEnrichmentObj.addInterlinkingResults(sourceModel,interlinkingResultModel);
        /*
        Property sameAs = model_out.createProperty(linkedRelationship);

        model_out.setNsPrefixes(model.getNsPrefixMap());
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object

            if(object.toString().equals("REZA")){
                Resource resource=model_out.createResource(subject.getNameSpace());
                resource.addProperty(predicate, object);
                resource.addProperty(sameAs,"Hello");
            }
            //RDFNode rdfNode=resource
            /*if (object instanceof Resource) {
                System.out.println(object.toString());

            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
        }
        FileOutputStream dumpFile=null;
        try{
            dumpFile=new FileOutputStream(dumpFileAddress);
        }catch(IOException e){
            System.out.println(e);
        }
        model_out.write(dumpFile,outputFileFormat);     */
    }
}
