import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/14
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */

public class RDB2RDF {

    public static OntModel sample_Model = null;

    // Ontology setting
    public static  String NameSpace = null;
    // where we've stashed it on disk for the time being
    protected static  String Ontology_Path = null;
    // the name of dataset
    protected static String DATASET_NAME = null;
    // the name of dump file
    protected static String DumpFileName = null;

    // Output setting
    // where the final dump is stored
    protected static  String OUTPUT_Folder = null;//"./src/output";
    // format of rdf dumps
    protected static String OUTPUT_Format = null;

    // where the log file is stored
    protected static  String LOG_Folder = null;
    protected static  String LOG_File = null;

    // Extension of rdf dumps
    protected static String OUTPUT_Extension = null;

    // Database setting
    protected static String ConnectionString = null;
    protected static String DatabaseName = null;
    protected static String MySQL_user = null;
    protected static String MySQL_password = null;

    // the port in which FUSEKI is run
    protected static String PORT = null;

    // config file name
    protected static String Config_file_path = "config.xml";

    protected static ArrayList<String> SQL_Queries_Array = new ArrayList<String>();
    protected static ArrayList<String> Properties_Array = new ArrayList<String>();
    protected static ArrayList<String> Column_Array = new ArrayList<String>();

    protected static Individual resource_individual=null;

    protected static String dcPrefix="dcterms";
    protected static String RDFPrefix="rdf";
    protected static String vCARD="VCARD";
    protected static String myprefix="myprefix";



    // ***************** Set prefix  *******************************************************
    public  void setRDFPrefix(){
        sample_Model.setNsPrefix(dcPrefix,"http://purl.org/dc/terms/");
        sample_Model.setNsPrefix(RDFPrefix,"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        sample_Model.setNsPrefix(vCARD,"http://www.w3.org/2001/vcard-rdf/3.0#");
    }

    // ***************** Get connection to SQL  *******************************************************
    public static Connection Get_Connection(String databaseName, String ConnectionString) throws Exception
    {
        try
        {
            System.out.println("Connecting to a selected database...");
            //  Name of database
            String connectionURL = ConnectionString+databaseName;
            Connection connection = null;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(connectionURL, MySQL_user, MySQL_password);
            System.out.println("Connected database successfully...");
            return connection;
        }
        catch (SQLException e)
        {
            System.out.println("Connection problem...");
            throw e;
        }
        catch (Exception e)
        {
            System.out.println("Connection problem...");
            throw e;
        }
    }

    //*************************** Loading OWL file into model  *****************************************
    protected static void loadModel() {

        sample_Model=ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );
        sample_Model.setNsPrefix(dcPrefix,"http://purl.org/dc/terms/");
        sample_Model.setNsPrefix(myprefix,"http://www.semanticweb.org/ontologies/2015/1/Ontology1424698842747.owl#");
        Model baseOntology = FileManager.get().loadModel( Ontology_Path );
        sample_Model.addSubModel(baseOntology);


    }

    //*************************** Reading configuration file  *****************************************
    public static void  readConfigurationFile(){
        try {

            File fXmlFile = new File(Config_file_path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Configuration");
            System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    ConnectionString=eElement.getElementsByTagName("connectionString").item(0).getTextContent().trim();
                    DatabaseName=eElement.getElementsByTagName("databaseName").item(0).getTextContent().trim();
                    MySQL_user=eElement.getElementsByTagName("username").item(0).getTextContent().trim();
                    MySQL_password=eElement.getElementsByTagName("password").item(0).getTextContent().trim();
                    Ontology_Path=eElement.getElementsByTagName("Ontology_Path").item(0).getTextContent().trim();
                    OUTPUT_Format=eElement.getElementsByTagName("OUTPUT_Format").item(0).getTextContent().trim();
                    OUTPUT_Folder=eElement.getElementsByTagName("OUTPUT_Folder").item(0).getTextContent().trim();
                    NameSpace=eElement.getElementsByTagName("NameSpace").item(0).getTextContent().trim();
                    DATASET_NAME=eElement.getElementsByTagName("DATASET_NAME").item(0).getTextContent().trim();
                    DumpFileName=eElement.getElementsByTagName("DumpFileName").item(0).getTextContent().trim();

                    NodeList SQL_Queries=eElement.getElementsByTagName("SQL");
                    NodeList Properties=eElement.getElementsByTagName("Property");
                    NodeList Column=eElement.getElementsByTagName("column");
                    for(int i=0;i<SQL_Queries.getLength();i++){
                        System.out.println("item "+i+"="+SQL_Queries.item(i).getTextContent().trim());
                        System.out.println("predicate "+i+"="+Properties.item(i).getTextContent().trim());
                        SQL_Queries_Array.add(SQL_Queries.item(i).getTextContent().trim());
                        Properties_Array.add(Properties.item(i).getTextContent().trim());

                        Column_Array.add(Column.item(i).getTextContent().trim());
                    }
                }
            }
            if(OUTPUT_Format.toLowerCase().equals("turtle"))
                OUTPUT_Extension="ttl";
            else if(OUTPUT_Format.toLowerCase().equals("n-triple"))
                OUTPUT_Extension="nt";
            else if(OUTPUT_Format.toLowerCase().equals("rdf/xml"))
                OUTPUT_Extension="rdf";
            else {
                System.out.println("The output format not found! please modify the config file.");
                System.exit(1);
            }
            if(ConnectionString.isEmpty() || DatabaseName.isEmpty() || MySQL_user.isEmpty() || MySQL_password.isEmpty() ){
                System.out.println("Something wrong in the config file!? Please read the instruction inside the config file");
                System.exit(1);
            }
            if(Ontology_Path.isEmpty() || OUTPUT_Format.isEmpty() || OUTPUT_Folder.isEmpty() || NameSpace.isEmpty() || DumpFileName.isEmpty()){
                System.out.println("Something wrong in the config file!? Please read the instruction inside the config file");
                System.exit(1);
            }
        } catch (FileNotFoundException NF) {
            System.out.println("config.xml not found ");
            System.exit(1);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------    Main function       ----------------
    public static  void RDB_read() {
        Connection conn = null;
        Statement stmt = null;
        ArrayList<String> resource_id = new ArrayList<String>();
        ArrayList<String> fileName = new ArrayList<String>();
        try{
            conn=Get_Connection(DatabaseName, ConnectionString);
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            stmt = conn.createStatement();
            String sqlValue=null;
            loadModel();
            OntClass sampleObject=sample_Model.getOntClass( "sampleObject" );
            resource_individual =sample_Model.createIndividual( NameSpace+"1", sampleObject );
            for(int counter=0;counter<SQL_Queries_Array.size();counter++) {
                sqlValue=SQL_Queries_Array.get(counter);
                ResultSet rs;
                rs= stmt.executeQuery(sqlValue);
                OntProperty property = sample_Model.getAnnotationProperty(Properties_Array.get(counter));
                System.out.println("sqlValue="+Properties_Array.get(counter));

                while (rs.next()) {
                    //System.out.println("Title="+title);
                    resource_individual.addProperty(property, rs.getString(Column_Array.get(counter)));
                }
                rs.close();
            }
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        FileOutputStream dumpFile=null;
        String dumpFileAddress=OUTPUT_Folder+"/"+DumpFileName+"."+OUTPUT_Extension;
        try{
            dumpFile=new FileOutputStream(dumpFileAddress);
        }catch(IOException e){
            System.out.println(e);
        }
        sample_Model.write(dumpFile, OUTPUT_Format);
    }
    // ---------------  MAIN  ----------------
    public static void main(String args[]) {
        readConfigurationFile();
        RDB_read();
    }
}
