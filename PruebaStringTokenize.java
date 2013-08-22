/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebastringtokenize;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;


/**
 *
 * @author Adrian
 */
public class PruebaStringTokenize {
    
    static int N=1033;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        
        //APERTURA DE FICHERO
        String ruta = "C:\\";
        List archivosNombre = new ArrayList();
        archivosNombre.add("MED.PART1");
        archivosNombre.add("MED.PART2");
        archivosNombre.add("MED.PART3");
        
        File archivo;
        RandomAccessFile br;
        
        HashMap indexMap = new HashMap();
        HashMap detalladoMap = new HashMap();
        HashMap correspondenciaMap = new HashMap();
        
        if(existeFichero("D:\\N.txt")){
            archivo = new File ("D:\\N.txt");
            br = new RandomAccessFile(archivo,"r");
            N = Integer.parseInt(br.readLine());
            br.close();
            System.out.println(N);
        }
        else{
                System.out.println(N);
                FichN();
        }
        
        //SI YA EXISTE INDICE LEEMOS
        if(existeFichero("D:\\INDICE.txt")){     
             correspondenciaMap = leerCorrespondencia();
             indexMap = leerIndice();
             detalladoMap = leerDetallado();
        }
        //SI NO EXISTE INDICE LO CREAMOS
        else{
            
            correspondenciaMap = crearCorrespondencia();
            
            for(int i=0;i<3;i++){
                archivo = new File (ruta+archivosNombre.get(i));
                //fr = new FileReader (archivo);
                br = new RandomAccessFile(archivo,"r");
                procesamientoFichero(br, indexMap,i+1, detalladoMap);
            }
           
            guardarIndice(indexMap);
            guardarDetallado(detalladoMap);
            guardarCorrespondencia(correspondenciaMap);
        }
        
        Pantalla pMenu = new Pantalla(indexMap, detalladoMap, correspondenciaMap);
        pMenu.show();

    }

    //======================================================
    // FUNCION QUE CREA EL INDICE
    //======================================================
    private static void procesamientoFichero(RandomAccessFile br, HashMap indexMap, int path, HashMap detalladoMap) throws IOException {
        String linea = "";
        String tratado = "";
        String word = "";
        String idDocument="";
        StringTokenizer st;
        long posicion = 0;
        int numlineas = 0;
        boolean primera = true;
                
        while((linea=br.readLine())!=null){
                    numlineas++; //INCREMENTO LINEA OJO
                    if(!((StringUtils.startsWith(linea, ".W"))||(StringUtils.startsWith(linea, ".I")))){
                        while(StringUtils.endsWith(linea, "-")){
                            linea = StringUtils.removeEnd(linea, "-");
                            linea += linea=br.readLine();
                        }
                        //TRATAMIENTO DE STRING
                        tratado = StringUtils.remove(linea, ","); 
                        tratado = StringUtils.remove(tratado, ".");
                        tratado = StringUtils.remove(tratado, ";");
                        tratado = StringUtils.remove(tratado, ":");
                        tratado = StringUtils.remove(tratado, "'"); 
                        tratado = StringUtils.remove(tratado, "/"); 
                        tratado = StringUtils.remove(tratado, "("); 
                        tratado = StringUtils.remove(tratado, ")"); 
                        tratado = StringUtils.remove(tratado, "?"); 
                        tratado = StringUtils.remove(tratado, "!"); 
                        tratado = StringUtils.remove(tratado, "\""); 
                        tratado = StringUtils.remove(tratado, "- ");
                        tratado = StringUtils.remove(tratado, " -");
                        tratado = StringUtils.remove(tratado, "+");
                        tratado = StringUtils.remove(tratado, "=");
                        tratado = StringUtils.remove(tratado, "<");
                        tratado = StringUtils.remove(tratado, ">");
                        tratado = StringUtils.remove(tratado, "%");
                        tratado = StringUtils.remove(tratado, "&");
                        tratado = StringUtils.remove(tratado, "$");
                        tratado = StringUtils.remove(tratado, "Â·");
                        tratado = StringUtils.remove(tratado, "#");

                        st = new StringTokenizer(tratado);

                        while (st.hasMoreTokens()) {
                            word = st.nextToken();  
                            word = word.toLowerCase(); //CONVERTIMOS A MINUSCULAS
                            
                             if(StringUtils.startsWith(word, "-")||StringUtils.endsWith(word, "-")){
                                    word = StringUtils.remove(word,"-");
                                }
                             
                            if(!StopWords.isStopword(word) && word.length()>1 && !StringUtils.isNumeric(word)){
                                   
                                Documento documento=new Documento();
                                List listDocument=new ArrayList();

                                if(indexMap.containsKey(word)){ //SI ESTA YA LA PALABRA
                                    listDocument = (List)indexMap.get(word);
                                    Iterator it=listDocument.iterator();
                                    boolean esta = false;
                                     while(it.hasNext() && esta == false){
                                            Documento doc=(Documento) it.next();
                                            if(doc.getIdentificador().equals(idDocument)){
                                                esta = true;
                                                doc.setOcurrencias(doc.getOcurrencias()+1);
                                            }      
                                     }
                                     if(esta==false){
                                        añadirDocumento(documento, idDocument, listDocument, indexMap, word);   
                                     }
                                }
                                else{                                     
                                    añadirDocumento(documento, idDocument, listDocument, indexMap, word); 
                                }
                            }
                        }  
                    }
                    else{
                        if(StringUtils.startsWith(linea, ".I")){
                            
                            if(primera==true){
                                primera = false;
                            }
                            else{
                                añadirDocumentoDetallado(detalladoMap, idDocument, posicion, numlineas, path);
                            }
                            posicion = br.getFilePointer();
                            tratado = StringUtils.remove(linea, ".I "); 
                            idDocument = tratado;
                            numlineas = 0;
                        }
                    }
        }
        añadirDocumentoDetallado(detalladoMap, idDocument, posicion, numlineas, path);
    }
    

    //======================================================
    // METODO QUE AÃ‘ADE UN DOCUMENTO AL INDEXMAP
    //======================================================
    public static void añadirDocumento(Documento documento, String idDocument, List listDocument, HashMap indexMap, String word) {
       documento.setIdentificador(idDocument); 
       documento.setOcurrencias(1);
       listDocument.add(documento); 
       indexMap.put(word, listDocument);
    }
    
    //======================================================
    // METODO QUE AÃ‘ADE UN DOCUMENTO DETALLADO
    //======================================================
    public static void añadirDocumentoDetallado(HashMap detalladoMap, String idDocument, long posicion, int lineas, int path) { 
       DetalleDocumento documento = new DetalleDocumento();
       documento.setPosicionB(posicion);
       documento.setLineas(lineas);
       documento.setPath(path);
       detalladoMap.put(idDocument, documento);
    }
    
    //======================================================
    // METODO QUE DICE SI UN FICHERO EXISTE
    //======================================================
    public static boolean existeFichero(String ruta){
        File archivo = new File (ruta);
        if (archivo.exists()){
            return true;
        }
        else{
            return false;
        } 
    }

    //======================================================
    // METODO QUE GUARDA EL INDICE EN UN FICHERO
    //======================================================
    private static void guardarIndice(HashMap indexMap) {
        FileWriter fichero = null;
        PrintWriter pw = null;
        String lineaIndice="";
        String word="";
        try
        {
            fichero = new FileWriter("D:/INDICE.txt");
            pw = new PrintWriter(fichero);

            Set indexKeys = indexMap.keySet(); //PARA PODER ITERAR
            Iterator it = indexKeys.iterator();
            while(it.hasNext()){
                 word = (String)it.next();
                 List listDocumentToPrint=(List)indexMap.get(word);
                 lineaIndice+=word;
                 Iterator it2=listDocumentToPrint.iterator();
                 while(it2.hasNext()){
                        Documento doc=(Documento) it2.next();
                        lineaIndice+= " "+doc.getIdentificador();
                        lineaIndice+= " "+doc.getOcurrencias(); 
                 }
                 pw.println(lineaIndice);
                 lineaIndice = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
    }

    //======================================================
    // METODO QUE LEE UN INDICE
    // Sera invocado siempre que exista ya el indice
    //======================================================
    private static HashMap leerIndice() {
        HashMap aux = new HashMap();
        File archivo = new File ("D:\\INDICE.txt");        
        FileReader fr = null;
        String linea="";
        StringTokenizer st; 
        String word;
        int iteraciones;
        Documento documento = new Documento();
        List listDocument = null;
        
        try {
            fr = new FileReader (archivo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PruebaStringTokenize.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader br = new BufferedReader(fr);
        
        try {
            while((linea=br.readLine())!=null){
                st = new StringTokenizer(linea);
                word = st.nextToken();
                while (st.hasMoreTokens()) {
                    listDocument = new ArrayList();
                    
                    iteraciones = (st.countTokens())/2;
                    
                    for(int i=0; i<iteraciones;i++){
                        documento = new Documento();
                        documento.setIdentificador(st.nextToken());
                        documento.setOcurrencias(new Integer(st.nextToken()));
                        listDocument.add(documento);                         
                    }        
                }
                aux.put(word, listDocument);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(PruebaStringTokenize.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aux;
    }
    
    //======================================================
    // UNA BUSQUEDA POR CADA PALABRA
    //======================================================
    public String tokenizarBusqueda(HashMap indexMap, HashMap detalladoMap, HashMap correspondenciaMap, String palabra) throws FileNotFoundException, IOException{
        String busqueda="";
        StringTokenizer tk = new StringTokenizer(palabra);
        do{
            String token = tk.nextToken();
            busqueda += buscarPalabra(indexMap,detalladoMap,correspondenciaMap,token);
        }while(tk.hasMoreElements());
        return busqueda;
    }
    
    
    //======================================================
    // METODO QUE BUSCA UNA PALABRA
    //======================================================
    public String buscarPalabra(HashMap indexMap, HashMap detalladoMap, HashMap correspondenciaMap, String palabra) throws FileNotFoundException, IOException {
        
        calculoRanking(indexMap,palabra);
        
        String informacion="";
        String aux="";
        String anterior="";
        palabra = palabra.toLowerCase(); //PASAMOS A MINUSCULAS
        List listDocumentToPrint=(List)indexMap.get(palabra);
        String nombreFichero = "";
        
        int iteraciones = 0;
        boolean encontrado = false;
        
        RandomAccessFile fichero;
        
        if(listDocumentToPrint == null){
            return "No se encontraron correspondencias a su busqueda.";
        }
                
        else{
            Iterator it=listDocumentToPrint.iterator();
            while(it.hasNext()){
                    encontrado = false;
                    iteraciones = 0;
                    Documento doc=(Documento) it.next(); //COJEMOS EL DOMCUMENTO
                    informacion += "Identificador: "+doc.getIdentificador()+"\n";    
                
                    DetalleDocumento det = (DetalleDocumento)detalladoMap.get(doc.getIdentificador());
                    //informacion += "Path: "+det.getPath()+"\n";
                    
                    nombreFichero = (String) correspondenciaMap.get(det.getPath());
                    //informacion += "Fichero: "+nombreFichero+"\n";
                    
                    fichero = new RandomAccessFile(nombreFichero,"r");
                    fichero.seek(det.getPosicionB());
                    
                    aux = fichero.readLine();
                    
                    informacion += "Titulo: "+fichero.readLine()+"\n";
                    
                    aux="";
                    
                    while(encontrado == false && aux!=null && iteraciones < det.getLineas()){
                        anterior = aux;
                        aux = fichero.readLine();
                        if(StringUtils.contains(aux, palabra)){
                            if(!anterior.equals("")){
                                informacion += "Resumen: "+anterior+"\n";
                                informacion += "                    "+aux+"\n";
                                aux = fichero.readLine();
                                if(!StringUtils.startsWith(aux, ".I")){
                                    informacion += "                    "+aux+"\n";
                                }
                            }
                            else{ 
                                informacion += "Resumen: "+aux+"\n";
                                aux = fichero.readLine();
                                if(!StringUtils.startsWith(aux, ".I")){
                                    informacion += "                    "+aux+"\n";
                                }
                            }
                        encontrado = true;
                        informacion += "\n";
                        iteraciones++;
                        }
                    }
                    
                    //        RandomAccessFile fichero = new RandomAccessFile ("C://MED.PART4", "r");
                    //        long ptr = fichero.getFilePointer();
                    //        System.out.println(ptr);
                    //        String aux = fichero.readLine();
                    //        System.out.println("PRIMERA: "+aux);
                    //        ptr = fichero.getFilePointer();
                    //        System.out.println(ptr);
                    //        aux = fichero.readLine();
                    //        System.out.println("SEGUNDA: "+aux);
                    //        aux = fichero.readLine();
                    //        System.out.println("TERCERA: "+aux);
                    //        fichero.seek(ptr);
                    //        aux = fichero.readLine();
                    //        System.out.println("encontrado: "+aux);
                    
             }
            return informacion;
        }
    }
    
    //======================================================
    // METODO QUE INDEXA UN NUEVO ARCHIVO
    // Pasamos como paremetro el nombre del fichero y el indexMap
    // simplemente llamamos a la funcion procesamientoFichero
    // y al terminar guardamos
    //======================================================
    public boolean indexarNuevo(HashMap indexMap, String ruta, HashMap detalladoMap, HashMap correspondenciaMap) throws FileNotFoundException{
        ruta = "C:\\"+ruta;
        if(existeFichero(ruta)){
            RandomAccessFile br = new RandomAccessFile(ruta,"r");
            RandomAccessFile brAux = new RandomAccessFile(ruta,"r");
            try {
                CuentaI(brAux);
                procesamientoFichero(br, indexMap, 4, detalladoMap);
                correspondenciaMap.put(4, ruta);
                guardarCorrespondencia(correspondenciaMap);
                FichN();
                
            } catch (IOException ex) {
                Logger.getLogger(PruebaStringTokenize.class.getName()).log(Level.SEVERE, null, ex);
            }
            guardarIndice(indexMap);
            guardarDetallado(detalladoMap);
            return true;
        }
        else{
            return false;
        }
    }
    
    //CREACION DEL FICHERO DE CORRESPONDENCIA
    public static HashMap crearCorrespondencia() throws IOException{
        HashMap correspondencia = new HashMap();
        correspondencia.put(1, "C:\\MED.PART1"); 
        correspondencia.put(2, "C:\\MED.PART2"); 
        correspondencia.put(3, "C:\\MED.PART3"); 
        return correspondencia;
    }
    
    public static void guardarCorrespondencia(HashMap correspondenciaMap) throws IOException{
        FileWriter fichero = new FileWriter("D:/CORRESPONDENCIA.txt");
        PrintWriter pw = new PrintWriter(fichero);
        Set indexKeys = correspondenciaMap.keySet(); //PARA PODER ITERAR
        Iterator it = indexKeys.iterator();
        while(it.hasNext()){
             pw.println(correspondenciaMap.get(it.next()));
        }
        fichero.close();
    }
    
    public static HashMap leerCorrespondencia() throws IOException{
        HashMap lectura = new HashMap();
        String linea="";
        
        File archivo = new File ("D:\\CORRESPONDENCIA.txt");        
        FileReader fr = new FileReader (archivo);
        BufferedReader br = new BufferedReader(fr);
        
        int i = 1;
        while((linea=br.readLine())!=null){
            linea = StringUtils.trim(linea);
            lectura.put(i, linea);
            i++;
        }
        
        return lectura;
    }
    
    public static void guardarDetallado(HashMap detalladoHash){
        FileWriter fichero = null;
        PrintWriter pw = null;
        String lineaDetallado="";
        String identificador="";
        DetalleDocumento documento = new DetalleDocumento();
        try
        {
            fichero = new FileWriter("D:/DETALLADO.txt");
            pw = new PrintWriter(fichero);

            Set indexKeys = detalladoHash.keySet(); //PARA PODER ITERAR
            Iterator it = indexKeys.iterator();
            while(it.hasNext()){
                 identificador = (String)it.next();
                 lineaDetallado+=identificador;
                 documento = (DetalleDocumento) detalladoHash.get(identificador);
                 lineaDetallado+= " "+documento.getPosicionB();
                 lineaDetallado+= " "+documento.getLineas(); 
                 lineaDetallado+= " "+documento.getPath();
                 pw.println(lineaDetallado);
                 lineaDetallado = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
    }
    
    
    //SIN TERMINAR
    public static HashMap leerDetallado(){
        HashMap aux = new HashMap();
        File archivo = new File ("D:\\DETALLADO.txt");        
        FileReader fr = null;
        String linea="";
        StringTokenizer st; 
        String id;
        DetalleDocumento documento = new DetalleDocumento();
   
        try {
            fr = new FileReader (archivo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PruebaStringTokenize.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader br = new BufferedReader(fr);
        
        try {
            while((linea=br.readLine())!=null){
                st = new StringTokenizer(linea);
                id = st.nextToken();
                while (st.hasMoreTokens()) {

                        documento = new DetalleDocumento();
                        documento.setPosicionB(new Long(st.nextToken()));
                        documento.setLineas(new Integer(st.nextToken()));
                        documento.setPath(new Integer(st.nextToken()));
      
                }
                aux.put(id, documento);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(PruebaStringTokenize.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aux;
    }
    
    public static void FichN() throws IOException {        
        FileWriter archivo2 = new FileWriter ("D:/N.txt");                               
        PrintWriter pw = new PrintWriter(archivo2);
        pw.println(N);
        pw.close();
    }
    
    public static void CuentaI(RandomAccessFile br) throws IOException{
        String linea;
        while((linea=br.readLine())!=null){
            if(StringUtils.startsWith(linea, ".I")) N++;
        }
    }
    
    //
    public void calculoRanking(HashMap indexMap, String palabra){
        List w = new ArrayList();
        Documento doc;
        int freqmax=0;
        List informacionPalabra = (List)indexMap.get(palabra);
        Iterator it = informacionPalabra.iterator();
        double tf;
        double idf = Math.log10(N/informacionPalabra.size());
        
        do{
            doc = (Documento) it.next();
            if(doc.getOcurrencias() > freqmax)
                freqmax = doc.getOcurrencias();
        }while(it.hasNext());
        
        Iterator it2 = informacionPalabra.iterator();
        do{
            doc = (Documento) it2.next();
            tf = 0.5 + ((0.5) * ((double)doc.getOcurrencias()/freqmax));
            w.add(tf * idf);
            System.out.println(tf * idf);
        }while(it2.hasNext());
        
    }
    
}
