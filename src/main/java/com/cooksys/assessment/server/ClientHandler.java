package com.cooksys.assessment.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);		

	private Socket socket;
    private final PrintWriter os;
    private final BufferedReader is;
    String username = "login";

    private final ConcurrentMap<String, ClientHandler> clients; 
   
    public ClientHandler(Socket socket, ConcurrentMap<String, ClientHandler> clients) throws IOException {
		super();
		this.socket = socket;
		this.clients = clients;
	      this.os = new PrintWriter(
	          new OutputStreamWriter(socket.getOutputStream()));
	      this.is = new BufferedReader(new InputStreamReader(socket
	          .getInputStream()));
	}  
  	
	public String time(){
		Calendar today = Calendar.getInstance();
		String time = today.get(Calendar.HOUR_OF_DAY)+":"+today.get(Calendar.MINUTE)+":"+today.get(Calendar.SECOND);	         
		return time;
	}
	public void run() {
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			
			while (!socket.isClosed()) {
				String raw = is.readLine();
				Message message = mapper.readValue(raw, Message.class);					

				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());	
						if(clients!=null){
						for (String key: clients.keySet())							
					      { if (key!=null){
							ClientHandler recipient = clients.get(key);								
							PrintWriter writer = new PrintWriter(
							          new OutputStreamWriter(recipient.socket.getOutputStream()));
							String gf = "{\r\n" +	
									  "\"time\": \"" + time()  + "\",\r\n" + 
									  "\"type\": \" The user \",\r\n" + 
									   "\"username\": \"" + message.getUsername() + "\",\r\n" + 
									   "\"contents\": \"has connected. \"\r\n" + 
								       "}" ;
										message = mapper.readValue(gf,  Message.class);
										String hy = mapper.writeValueAsString(message);							    	 										   
										writer.write(hy);
										writer.flush();}
							     
									 }}
						clients.put(message.getUsername(), this);	
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						clients.remove(message.getUsername());
						if(clients!=null){
							for (String key: clients.keySet())							
						      { if (key!=null){
								ClientHandler recipient = clients.get(key);								
								PrintWriter writer = new PrintWriter(
								          new OutputStreamWriter(recipient.socket.getOutputStream()));
								String gf = "{\r\n" +	
										  "\"time\": \"" + time()  + "\",\r\n" + 
										  "\"type\": \" The user \",\r\n" + 
										   "\"username\": \"" + message.getUsername() + "\",\r\n" + 
										   "\"contents\": \"has disconnected. \"\r\n" + 
									       "}" ;
											message = mapper.readValue(gf,  Message.class);
											String hy = mapper.writeValueAsString(message);							    	 										   
											writer.write(hy);
											writer.flush();}
								     
										 }}
						this.socket.close();
						break;
						
					case "echo":						
						 String echoM = "{\r\n" +	
								  "\"time\": \"" + time()  + "\",\r\n" + 
								  "\"type\": \" (echo): \",\r\n" + 
					               "\"username\": \"" + message.getUsername() + "\",\r\n" + 
					               "\"contents\": \""+  message.getContents() +"\"\r\n" +  
					               "}" ;
						message = mapper.readValue(echoM,  Message.class);
						String e = mapper.writeValueAsString(message);										   
						os.write(e);
						os.flush();
						break;
						
					case "users":	        	
						String name = "";
						for (String key: clients.keySet())			
							name = name + "  "+key;	
							String str = "{\r\n" +	
							"\"time\": \"" + time()  + "\",\r\n" + 
							 "\"type\": \" : \",\r\n" + 
							"\"username\": \"currently connected users: \",\r\n" +  
							 "\"contents\": \""+ name +"\"\r\n" + 
							               "}" ;
							message = mapper.readValue(str,  Message.class);
							String ui = mapper.writeValueAsString(message);									   
							os.write(ui) ;
							os.flush();
							break;	
							
					case "broadcast":
						try{
						for (String key: clients.keySet())							
					      { if (key!=null){
							ClientHandler recipient = clients.get(key);								
							PrintWriter writer = new PrintWriter(
							          new OutputStreamWriter(recipient.socket.getOutputStream()));
							String gf = "{\r\n" +	
									  "\"time\": \"" + time()  + "\",\r\n" + 
									  "\"type\": \" (all): \",\r\n" + 
									   "\"username\": \"" + message.getUsername() + "\",\r\n" + 
									   "\"contents\": \""+  message.getContents() +"\"\r\n" + 
								       "}" ;
										message = mapper.readValue(gf,  Message.class);
										String hy = mapper.writeValueAsString(message);							    	 										   
										writer.write(hy);
										writer.flush();
							     
									 }}}
						catch(IOException h){
							log.error("Something went wrong :/", h);}
						break;
							
					default: try{username = message.getCommand();
					           if(clients.containsKey(username)){						
								
								ClientHandler recipient = clients.get(username);								
								PrintWriter writer = new PrintWriter(
								          new OutputStreamWriter(recipient.socket.getOutputStream()));
								String gf = "{\r\n" +	
										  "\"time\": \"" + time()  + "\",\r\n" + 
										  "\"type\": \" (whisper): \",\r\n" + 
										   "\"username\": \"" + message.getUsername() + "\",\r\n" + 
										   "\"contents\": \""+  message.getContents() +"\"\r\n" + 
									       "}" ;
											message = mapper.readValue(gf,  Message.class);
											String hy = mapper.writeValueAsString(message);							    	 										   
											writer.write(hy);
											writer.flush();
					}
					else {String string = "{\r\n" +	
							"\"time\": \"" + time()  + "\",\r\n" + 
							 "\"type\": \" The command \",\r\n" + 
							 "\"username\": \"" + message.getCommand() + "\",\r\n" + 
							"\"contents\": \"was not recognized \"\r\n" + 
							               "}" ;
							message = mapper.readValue(string,  Message.class);
							String hh = mapper.writeValueAsString(message);									   
							os.write(hh) ;
							os.flush();}
						
				}
			catch(IOException ex){
				log.error("Something went wrong :/", ex);}
				}
			}} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
			
}}
		

	      

