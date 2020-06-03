package com.google.sps.servlets;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.sps.data.Post;


@WebServlet("/discussion")
public class DiscussionServlet extends HttpServlet {

  private ArrayList<Post> posts;

  @Override
  public void init() {
    posts = new ArrayList<Post>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    if (posts.isEmpty()){
      response.setContentType("text/html;");

      response.getWriter().println("No discussions yet.");
    
    } else {

      for (Post post : posts){
        
        Query query = new Query(post.getTitle()).addSort("commentTime", SortDirection.ASCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery result = datastore.prepare(query);

        for (Entity entity : result.asIterable()) {
          String postTitle = (String) entity.getProperty("postTitle");
          String postContent = (String) entity.getProperty("postContent");
        }
          
      }

      String json = convertToJson(classicRock);

      response.setContentType("application/json;");
      response.getWriter().println(json);
    }

  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String postTitle = getParameter(request, "post-title", "");
    String postContent = getParameter(request, "post-content", "");

    Post newPost = new Post(postTitle, postContent);
    posts.add(newPost);

    Entity post = new Entity(newPost.getTitle());
    post.setProperty("postTitle", newPost.getTitle());
    post.setProperty("postContent", newPost.getContent());
    post.setProperty("commentTime", System.currentTimeMillis());

    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    dataStore.put(post);

    response.sendRedirect("/discussion.html");
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);

    if (value == null) {
      return defaultValue;
    }
    return value;

  }


  private String convertToJson(ArrayList<String> data) {
    Gson gson = new Gson();
    String json = gson.toJson(data);
    return json;
  }
}
