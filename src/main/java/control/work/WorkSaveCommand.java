package control.work;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import control.ConnectionSingleton;
import control.JSONData;
import control.Print;
import control.area.AreaControl;
import control.auth.AuthControl;
import control.keywords.KeywordsControl;
import model.JSONOut;
import model.Keyword;
import model.Obra;

public class WorkSaveCommand extends WorkCommand{

	@Override
	public void execute() throws Exception {
		@SuppressWarnings("deprecation")
		boolean isMultiPart = FileUpload.isMultipartContent(getRequest());
		
		String user = null;
		
        JSONData data = new JSONData().create();

        if (isMultiPart) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            @SuppressWarnings("unused")
			String formulario = "";
	        @SuppressWarnings("rawtypes")
			List items = upload.parseRequest(getRequest());
	        @SuppressWarnings("rawtypes")
			Iterator iter = items.iterator();
	        
	        Obra work = Obra.newInstance();
	        Keyword keyword = Keyword.newInstance();
	
	        while (iter.hasNext()) {
	            FileItem item = (FileItem) iter.next();
	            String field = item.getFieldName();
	            String value = item.getString("UTF-8");
	            // Others inputs from form
	            if(field.equals("type")){
	            	work.setType(Integer.valueOf(value));
	            }else if(field.equals("title")){
	            	work.settitulo(value);
	            }else if(field.equals("institution")){
	            	work.setinstituicao(value);
	            }else if(field.equals("auth")){
	            	work.setautor(value);
	            }else if(field.equals("area")){
	            	work.setarea(value);
	            }else if(field.equals("date")){
	            	work.setdata(value);
	            }else if(field.equals("resume")){
	            	work.setresumo(value);
	            }else if(field.equals("user")){
	            	user = value;
	            }else if(field.equals("keywords")){
	            	keyword.setWords(value.toLowerCase());
	            }else if (item.getFieldName().equals("file")) { // Input file 
	                formulario = item.getString();
	                
	                if(!item.isFormField()){
	                	if (item.getName().length() > 0) {
	                		// Save work into directory e save into table
		                    try{
		                    	save(item, work, keyword, user);
		                    	data.put(JSONOut.CODE, JSONOut.Sucess.COMPLETADA);
		                    }catch(Exception e){
		                    	e.printStackTrace();
		                    	data.put(JSONOut.CODE, JSONOut.Erro.OCORREU_ALGUM_ERRO);
		                    }
		                }
	                }
	            }
	            //System.out.println("Verificando os campos enviados : " + item.getFieldName() + "," + item.isFormField());
	        }
        }
        
        // Mostra a saida em JSON
     	Print.json(getResponse(), data);
	}
            
    private void save(FileItem item, Obra work, Keyword keyword, String usuario) 
    		throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

    	// Save work into directory and generate full path
    	String path = WorkControl.saveFile(getRequest(), item);
		
		// Insere a area no banco, verificando se ela já existe
		Long areaId = saveArea(work);
		System.out.println("ID area : " + areaId.toString());
		
		// Insert auth into table and generate ID 
		Long authId = saveAuth(work);
		System.out.println("ID auth : " + authId.toString());

		//Guarda no banco de dados o endereço para recuperação da imagem
		Long idObra = ObraDao.insereObra
		(
				work.getType(),
				work.getinstituicao(),
				areaId.toString(), 
				authId.toString(), 
				work.gettitulo(), 
				work.getdata(), 
				work.getresumo(), 
				path, 
				usuario
		);
		
    	// Salva as palvras chaves da obra
    	saveKeywords(keyword, idObra);
	}
    
    
    private void saveKeywords(Keyword keyword, Long idObra) {
    	Connection conn = ConnectionSingleton.getInstance().getConnection();
    	
    	for(int i=0, total = keyword.getWords().length; i<total; i++){    		
    		String word = keyword.getWords()[i];
    		Long Idpalavrachave = null;
    		word = word.trim();
    		try {
    			// Tenta adicionar a palavra chave no banco
    			Idpalavrachave = KeywordsControl.add(conn, word);
			} catch (Exception e) {
				// A palavra chave ja existe
				//e.printStackTrace();
				System.out.println("A palavra chave ja existe para a obra " + idObra);
				try {
					Idpalavrachave = KeywordsControl.findByName(conn, word);
				} catch (Exception e1) {
					// Nao foi possivel encontrar a palavra chave
					//e1.printStackTrace();
					System.out.println(String.format("Não foi possivel encontrar a chave '%s' ja existe para obra %s", idObra, word));
				}
			}
    		
    		if(Idpalavrachave != null){
    			try {
    				// Adiciona palavra chave para a obra se nao existir
					KeywordsControl.addKeywordAndWork(conn, Idpalavrachave, idObra);
				} catch (SQLException e) {
					// A palavra chave ja existe para esta obra
					//e.printStackTrace();
					System.out.println("Não foi possivel encontrar a chave ja existe para obra " + idObra);
				}
    		}
    	} // Fim for
	}

	public static Long saveAuth(Obra work) {
    	Connection conn = ConnectionSingleton.getInstance().getConnection();
    	String auth = work.getautor();
    	Long authId = null;
    	
    	System.out.println("Auth : " + auth);
		
		try {
			authId = AuthControl.add(conn, auth);
		} catch (Exception e) {
			// Auth already exists
			try {
				authId = AuthControl.findByName(conn, auth);
			} catch (Exception e1) {
				// Could not possible get ID from auth
				e1.printStackTrace();
			}
		}
    	
		return authId;
	}

	private Long saveArea(Obra work){
    	Connection conn = ConnectionSingleton.getInstance().getConnection();
		String area = work.getarea();
		Long areaId = null;
		
		System.out.println("Area : " + area);
		
		try {
			areaId = AreaControl.add(conn, area);
		} catch (Exception e) {
			// A obra ja existe 
			try {
				areaId = AreaControl.findByName(conn, area);
			} catch (Exception e1) {
				// Nao foi possivel recuperar o ID
				e1.printStackTrace();
			}
		}
		
		return areaId;
    }

}

	
