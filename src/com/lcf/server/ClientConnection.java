package com.lcf.server;

import java.io.BufferedReader;
import java.util.*;
import java.text.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientConnection implements Runnable {

	private Socket clientSocket;

	public ClientConnection(Socket sock) {
		clientSocket = sock;
	}

	@SuppressWarnings("resource")
	public void run() {
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
					true);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);

				String[] commands = inputLine.split(":;:");
				if (commands[0].equals("app")) {
					String reply = "";
					Statement stat = DBConnection.getDBConnection()
							.createStatement();
					if (commands[1].equals("login")) {
						String query = "SELECT UID FROM user WHERE UID = \""
								+ commands[2] + "\" and PASSWORD =\""
								+ commands[3] + "\"";
						ResultSet rset = stat.executeQuery(query);
						ResultSetMetaData rsmd = rset.getMetaData();

						String user = null;
						while (rset.next()) {
							for (int i = 1; i <= rsmd.getColumnCount(); i++) {
								user = rset.getString(i);
							}
						}

						if (user == null) {
							reply = "FAIL";
						} else if (user.equals(commands[2])) {
							reply = "SUCCESS";
						} else {
							System.out.println(commands[2] + user);
							reply = "FAIL";
						}

					} else {
						String query = "SELECT FUNC FROM iBeacon WHERE UUID = \""
								+ commands[2]
								+ "\" and major = \""
								+ commands[3]
								+ "\" and minor = \""
								+ commands[4] + "\"";

						ResultSet rset = stat.executeQuery(query);
						ResultSetMetaData rsmd = rset.getMetaData();

						String func = null;
						while (rset.next()) {
							for (int i = 1; i <= rsmd.getColumnCount(); i++) {
								func = rset.getString(i);
							}
						}

						if (func == null) {
							reply = "NoAction";
						} else if (func.equals("L")) {
							if (commands[5].equals("far")
									|| commands[5].equals("near")
									|| commands[5].equals("immediate")) {

								Date dNow = new Date();
								SimpleDateFormat ft = new SimpleDateFormat(
										"yyyy-MM-dd");
								String today = ft.format(dNow);
								System.out.println(today);

								rset = stat
										.executeQuery("SELECT UID FROM reservation where HID = \""
												+ commands[3]
												+ "\" and UID = \""
												+ commands[1]
												+ "\" and CHECKINDATE <= \""
												+ today
												+ "\" and CHECKOUTDATE >= \""
												+ today + "\"");
								System.out.println("SELECT UID FROM reservation where HID = \""
										+ commands[3]
										+ "\" and UID = \""
										+ commands[1]
										+ "\" and CHECKINDATE <= \""
										+ today
										+ "\" and CHECKOUTDATE >= \""
										+ today + "\"");

								if (rset.first()) {

									rset = stat
											.executeQuery("SELECT HNAME,HINFO FROM hotel where HID = \""
													+ commands[3] + "\"");

									rsmd = rset.getMetaData();

									func = null;
									reply = "checkin";
									while (rset.next()) {
										for (int i = 1; i <= rsmd
												.getColumnCount(); i++) {
											System.out.println(rset
													.getString(i));
											reply = reply + ":;:"
													+ rset.getString(i);
										}
									}
								} else {
									reply = "NoAction";
								}

							} else if (commands[5].equals("checkin")) {
								rset = stat
										.executeQuery("select RID from room where HID = \""
												+ commands[3]
												+ "\" and OCCUPIED = 0 and RID <> \"0000\"");
								String selectedRoom = null;
								rsmd = rset.getMetaData();

								//String func = null;
								while (rset.next()) {
									for (int i = 1; i <= rsmd.getColumnCount(); i++) {
										selectedRoom = rset.getString(i);
									}
								}
								
								if(selectedRoom == null){
									reply = "NoAction";
								} else {
									
									Date dNow = new Date();
									SimpleDateFormat ft = new SimpleDateFormat(
											"yyyy-MM-dd");
									String today = ft.format(dNow);
									System.out.println(today);
									
								stat
										.executeUpdate("update room set OCCUPIED = 1 where HID = \""
												+ commands[3]
												+ "\" and RID = \""
												+ selectedRoom + "\"");

								stat
										.executeUpdate("update reservation set RID = \""
												+ selectedRoom
												+ "\" where UID = \""
												+ commands[1]
												+ "\" and HID = \""
												+ commands[3] + "\" and CHECKINDATE = \"" + today + "\"");
								
								/*System.out.println("update reservation set RID = \""
										+ selectedRoom
										+ "\" where UID = \""
										+ commands[1]
										+ "\" and HID = \""
										+ commands[3] + "\" and CHECKINDATE = \"" + today + "\"");*/

								reply = "checkin:;:" + selectedRoom;
								}
							} else if (commands[5].equals("checkout")) {
								Date dNow = new Date();
								SimpleDateFormat ft = new SimpleDateFormat(
										"yyyy-MM-dd");
								String today = ft.format(dNow);
								
								rset = stat
										.executeQuery("select RID from reservation where UID = \""
												+ commands[1]
												+ "\" and CHECKOUTDATE = \"" + today + "\"");

								
								if(rset.first()){
									String selectedRoom = rset.getString(1);
									stat
											.executeUpdate("update room set OCCUPIED = 0 where HID = \""
													+ commands[3]
													+ "\" and RID = \""
													+ selectedRoom + "\"");
									reply = "checkout:;:" + selectedRoom;
								} else {
									reply = "NoAction";
								}
							}else if (commands[5].equals("cancheckout")) {
								Date dNow = new Date();
								SimpleDateFormat ft = new SimpleDateFormat(
										"yyyy-MM-dd");
								String today = ft.format(dNow);
								
								rset = stat
										.executeQuery("select RID from reservation where UID = \""
												+ commands[1]
												+ "\" and CHECKOUTDATE = \"" + today + "\"");

								
								if(rset.first()){
									reply = "checkout";
								} else {
									reply = "NoAction";
								}
							} else {
								reply = "NoAction";
							}

						} else if (func.equals("R")
								&& commands[5].equals("immediate")) {
							rset = stat
									.executeQuery("select UID from reservation where HID = \""
											+ commands[3]
											+ "\" and RID = \""
											+ commands[4] + "\"");
							if ((rset.first() && rset.getString(1).equals(
									commands[1]))) {
								Socket iBeaconSock = IBeacon.getSock(
										commands[3], commands[4]);
								PrintWriter iBeaconout = new PrintWriter(
										iBeaconSock.getOutputStream(), true);
								iBeaconout.println("OPEN");
								reply = "OPEN";
							} else {
								reply = "NoAction";
							}

						} else {
							reply = "NoAction";
						}
						rset.close();

					}

					out.println(reply);
					System.out.println("reply" + reply);

				} else if (commands[0].equals("iBeacon")) {
					IBeacon.addIBeacon(commands[2], commands[3], clientSocket);
					out.println("OPEN");
					/*
					 * while(true){ try { Thread.sleep(1000); } catch
					 * (InterruptedException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); } }
					 */
				} else {
					out.write("Error");
					break;
				}
			}

			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
