package view;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import control.RequestManager;

/**
 * Servlet implementation class NewWorkServlet
 * URL : {PATH}/obra/alterar{PARAMS}
 */
@WebServlet(name = "AlterarObra", urlPatterns = { "/obra/alterar" })
public class UpdateWorkServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateWorkServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Executa o comando para atualizar uma obra
		try {
			RequestManager.getInstance()
			.action(RequestManager.ALTERAR_OBRA)
			.setResponse(response)
			.setRequest(request)
			.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
