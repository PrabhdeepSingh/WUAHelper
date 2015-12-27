package com.xplosivegames.wuahelper;

import java.rmi.Naming;
import java.rmi.RemoteException;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.webinterface.WebInterface;

public class WUAhelper {
	
	private static WebInterface wurm;
	
	public static void main(String[] args) {
		String ip = args[0];
		String port = args[1];
		String rmiPassword = args[2];
		String doing = args[3];
		String[] paramters = args[4].split(",", -1);
		String connectionString = "//" + ip + ":" + port + "/wuinterface";
		try {
			wurm = (WebInterface) Naming.lookup(connectionString);
			switch(doing) {
				case "playerCount":
					System.out.println(getPlayerCount(rmiPassword));
					break;
				case "broadcast":
					System.out.println(broadcastMessage(rmiPassword, paramters[0].toString()));
					break;
				case "uptime":
					System.out.println(getUpTime(rmiPassword));
					break;
				case "wurmTime":
					System.out.println(getWurmTime(rmiPassword));
					break;
				case "isRunning":
					System.out.println(isRunning(rmiPassword));
					break;
				case "shutDown":
					System.out.println(shutDown(rmiPassword, paramters[0].toString(), Integer.parseInt(paramters[1].toString()), paramters[2].toString()));
					break;
				case "ban":
					System.out.println(banPlayer(rmiPassword, paramters[0].toString(), paramters[1].toString(), paramters[2].toString(), Integer.parseInt(paramters[3])));
					break;
				case "pardon":
					System.out.println(pardonPlayer(rmiPassword, paramters[0].toString(), paramters[1].toString()));
					break;
				case "mutePlayer":
					System.out.println(mutePlayer(rmiPassword, paramters[0].toString(), paramters[1].toString(), Integer.parseInt(paramters[2])));
					break;
				case "unMutePlayer":
					System.out.println(unmutePlayer(rmiPassword, paramters[0].toString()));
					break;
				case "addMoney":
					System.out.println(addMoney(rmiPassword, paramters[0].toString(), Long.parseLong((paramters[1]))));
					break;
				case "changePower":
					System.out.println(changePower(rmiPassword, paramters[0].toString(), Integer.parseInt((paramters[1]))));
					break;
				case "addItem":
					System.out.println(addItem(rmiPassword, paramters[0].toString(), Integer.parseInt((paramters[1])), Float.parseFloat((paramters[2])), Byte.parseByte((paramters[3])), paramters[4].toString(), Integer.parseInt((paramters[5]))));
					break;
				case "changeKingdom":
					System.out.println(changeKingdom(rmiPassword, paramters[0].toString(), Integer.parseInt(paramters[1])));
					break;
				default:
					System.out.println("No valid method supplied");
					break;
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Gets a player count on the server
	 * @return Integer Player count
	 * @throws RemoteException
	 */
	private static int getPlayerCount(String rmiPassword) throws RemoteException {
		return wurm.getPlayerCount(rmiPassword);
	}
	
	/**
	 * Broadcasts a message through the server
	 * @param message
	 * @return Boolean
	 */
	private static boolean broadcastMessage(String rmiPassword, String message) {
		try {
			wurm.broadcastMessage(rmiPassword, message);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the server uptime
	 * @return
	 * @throws RemoteException
	 */
	private static String getUpTime(String rmiPassword) throws RemoteException {
		return wurm.getUptime(rmiPassword);
	}

	/**
	 * Gets current wurm time
	 * @return
	 * @throws RemoteException
	 */
	private static String getWurmTime(String rmiPassword) throws RemoteException {
		return wurm.getWurmTime(rmiPassword);
	}
	
	/**
	 * Starts a server shutdown
	 * @param user
	 * @param seconds
	 * @param reason
	 * @return
	 */
	private static boolean shutDown(String rmiPassword, String user, int seconds, String reason) {
		try {
			wurm.startShutdown(rmiPassword, user, seconds, reason);
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks if the server / RMI is still running
	 * @return
	 * @throws RemoteException
	 */
	private static boolean isRunning(String rmiPassword) throws RemoteException {
		return wurm.isRunning(rmiPassword);
	}
	
	/**
	 * Bans a player (Doesn't kick them from the server)
	 * @param playerName
	 * @param ip
	 * @param reason
	 * @param days
	 * @return Boolean
	 * @throws RemoteException 
	 * @throws NoSuchPlayerException 
	 */
	private static boolean banPlayer(String rmiPassword, String playerName, String ip, String reason, int days) {
		try {
			if(wurm.wuaBan(rmiPassword, playerName, ip, reason, days)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unbans / pardons a player and IP
	 * @param playerName
	 * @param ip
	 * @return Boolean
	 */
	private static boolean pardonPlayer(String rmiPassword, String playerName, String ip) {
		try {
			wurm.removeBannedIp(rmiPassword, ip);
			wurm.pardonban(rmiPassword, playerName);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Mutes a player from taking in-game
	 * @param playerName
	 * @param reason
	 * @param hours
	 * @return Boolean
	 */
	private static boolean mutePlayer(String rmiPassword, String playerName, String reason, int hours) {
		
		try {
			if(wurm.wuaMute(rmiPassword, playerName, reason, hours)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gives a player his/her voice back 
	 * @param playerName
	 * @return
	 */
	private static boolean unmutePlayer(String rmiPassword, String playerName) {
		
		try {
			if(wurm.wuaUnMute(rmiPassword, playerName)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Adds money to a players bank account
	 * @param playerName
	 * @param amount
	 * @return Boolean
	 */
	private static boolean addMoney(String rmiPassword, String playerName, long amount) {
		try {
			wurm.addMoneyToBank(rmiPassword, playerName, amount, "");
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean changePower(String rmiPassword, String playerName, int power) {
		try {
			if(wurm.wuaChangePower(rmiPassword, playerName, power)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean addItem(String rmiPassword, String playerName, int itemTemplateID, float itemQuality, byte itemRarity, String creator, int itemAmount) {
		try {
			if(wurm.wuaGiveItem(rmiPassword, playerName, itemTemplateID, itemQuality, itemRarity, creator, itemAmount)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean changeKingdom(String rmiPassword, String playerName, int newKingdom) {
		try {
			if(wurm.wuaChangeKingdom(rmiPassword, playerName, newKingdom)) {
				return true;
			}
			else {
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
}
