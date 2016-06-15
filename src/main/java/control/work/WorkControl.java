package control.work;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONArray;

import control.Conversor;
import model.JSONOut;
import model.UserWork;

public class WorkControl {

	public static void findById(WorkDao dao, Object[] o) {
		// Transforma em JSON a saida
		JSONArray resultQuery;
		try {
			UserWork userWork = (UserWork) o[1];
			
			Long work = userWork.getWork().getId();
			Long user = userWork.getUser().getId();
			
			// cria um preparedStatement
			String sql = String.format("SELECT a.*, b.avaliacao FROM (SELECT a.id, a.autor, a.resumo, a.data, LCASE(a.titulo) as titulo, a.imagem as path, b.nome as area, c.nome as instituicao FROM obra a, area b, instituicao c WHERE a.area=b.id AND c.id = a.instituicao AND a.id = %s) a LEFT OUTER JOIN (SELECT * FROM avaliacao WHERE obra = %s AND usuario = %s) b ON a.id = b.obra", work, work, user);
			PreparedStatement stmt = dao.getCon().prepareStatement(sql);
			
			// Query com os dados do usuario
			ResultSet resultSet = stmt.executeQuery();

			resultQuery = Conversor.convertToJSON(resultSet);
			
			/**
			 *  Gera um objeto do tipo 
			 *  { 
			 *  	id : int, 
			 *  	autor : string, 
			 *  	resumo : string,  
			 *  	data : timestamp,
			 *  	titulo : string,
			 *  	area : string,
			 *  	instituicao : string,
			 *  	avaliacao : int
			 *  }
			 */
			dao.getData().put(JSONOut.DATA, resultQuery);
			
			//System.out.println("JSON : " + json.toString());
			
			stmt.close();
		} catch (SQLException e) {
			dao.getData()
			.put(JSONOut.CODE, JSONOut.Work.NENHUMA_OBRA_ENCONTRADA)
			.put(JSONOut.DATA, null);
			e.printStackTrace();
		} catch (Exception e) {
			dao.getData()
			.put(JSONOut.CODE, JSONOut.Work.NAO_FOI_POSSIVEL_ENCONTRAR_ESTA_OBRA)
			.put(JSONOut.DATA, null);
			e.printStackTrace();
		}
	}

	public static String saveFile(HttpServletRequest httpServletRequest, FileItem item) throws IOException {
		System.out.println("Subindo a obra para o servidor...");
    	
		//Pega o diretório /obras dentro do diretório atual de onde a aplicação está rodando
		String caminho = httpServletRequest.getServletContext().getRealPath("/obras") + "/";
		
		// Cria o diretório caso ele não exista
		File diretorio = new File(caminho);
		
		System.out.println("Path : " + caminho);
		
		if (!diretorio.exists()){
			diretorio.mkdir();
		}
		
		// Mandar o arquivo para o diretório informado
		Calendar cal = Calendar.getInstance();
		
		String nome =  cal.get(Calendar.DAY_OF_MONTH)+ "_" + cal.get(Calendar.MONTH) + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.HOUR_OF_DAY) + "_" + cal.get(Calendar.MINUTE) + "_" + cal.getTimeInMillis() + ".pdf";
		
		System.out.println("Nome : " + nome);
		
		File file = new File(diretorio, nome);
		FileOutputStream output = new FileOutputStream(file);
		
		InputStream is = item.getInputStream();
		
		byte[] buffer = new byte[2048];
		
		int nLidos;
		
		System.out.println("Absolute Path : " + file.getAbsolutePath());
		
		while ((nLidos = is.read(buffer)) >= 0) {
			output.write(buffer, 0, nLidos);
		}
		
		output.flush();
		output.close();
		
		return file.getAbsolutePath();
	}
}
