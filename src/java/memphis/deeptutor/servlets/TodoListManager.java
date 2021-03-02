package memphis.deeptutor.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.model.TodoItem;
import memphis.deeptutor.singleton.DerbyConnector;


/**
 * Servlet implementation class TasksManager
 */
@WebServlet("/todo")
public class TodoListManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TodoListManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);

	}

	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String command = (String)request.getParameter("command");
		
		if (command == null || command.equals(""))
		{
			System.out.println("Loading todo list...");

			//first time, load the todo items
			session.setAttribute("feedback", "Loading tasks.");
			session.setAttribute("todoItems", DerbyConnector.getInstance().GetTodoItems());
		}
		else{
			if (command.equals("create"))
			{
				System.out.println("Creating todo...");

				String creator = request.getParameter("creator");
				String assignee = request.getParameter("assignee");
				String text = request.getParameter("textItem");
				TodoItem item = new TodoItem();
				item.setCreator(creator);
				item.setAssignee(assignee);
				item.setText(text.replaceAll("'", "`"));
				int itemID = DerbyConnector.getInstance().CreateTodoItem(item);
				session.setAttribute("feedback", "ToDo task #"+itemID+" succesfully created.");
				session.setAttribute("todoItems", DerbyConnector.getInstance().GetTodoItems());
			}
			else
			{
				int itemID = Integer.parseInt(request.getParameter("itemID"));
				boolean success = false;
				if (command.equals("update"))
				{
					String responseItem = request.getParameter("responseItem").replaceAll("'", "`");
					success = DerbyConnector.getInstance().UpdateTodoItem(itemID, responseItem);

					session.setAttribute("todoItems", DerbyConnector.getInstance().GetTodoItems());
				}
				if (command.equals("close"))
				{
					String creatorKey = request.getParameter("creator");
					success = DerbyConnector.getInstance().CloseTodoItem(itemID, creatorKey);
				}
				
				if (success)
				{
					session.setAttribute("feedback", "Database succesfully updated");
					session.setAttribute("todoItems", DerbyConnector.getInstance().GetTodoItems());
				}
				else session.setAttribute("feedback", "Problem encountered when updating the database.");
			}
			
		}
		
		String adminPage = "/todo.jsp";
		RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
		rd.forward(request, response);
	}

}
