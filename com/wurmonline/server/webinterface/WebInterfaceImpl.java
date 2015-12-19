package com.wurmonline.server.webinterface;

import com.wurmonline.server.Constants;
import com.wurmonline.server.CounterTypes;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.VehicleSettings;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.BannedIp;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.questions.NewsInfo;
import com.wurmonline.server.questions.WurmInfo;
import com.wurmonline.server.questions.WurmInfo2;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcDeleteKingdom;
import com.wurmonline.server.webinterface.WcKingdomInfo;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.webinterface.WebInterfaceTest;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebInterfaceImpl extends UnicastRemoteObject
		implements WebInterface, Serializable, MiscConstants, TimeConstants, CounterTypes, MonetaryConstants {
	public static final String VERSION = "$Revision: 1.54 $";
	public static String mailAccount = "mail@mydomain.com";
	public static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[\\w\\.\\+-=]+@[\\w\\.-]+\\.[\\w-]+$");
	private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
	private static final long serialVersionUID = -2682536434841429586L;
	private final boolean isRunning = true;
	private final Random faceRandom = new Random();
	private static final long faceRandomSeed = 8263186381637L;
	private static final DecimalFormat twoDecimals = new DecimalFormat("##0.00");
	private static final Set moneyDetails = new HashSet();
	private static final Set timeDetails = new HashSet();
	private static final Logger logger = Logger.getLogger(WebInterfaceImpl.class.getName());
	private static final long[] noInfoLong = new long[] { -1L, -1L };
	private static final String BAD_PASSWORD = "Access denied.";
	private final SimpleDateFormat alloformatter = new SimpleDateFormat("yy.MM.dd\'-\'hh:mm:ss");
	private String hostname = "localhost";
	private static final Map ipAttempts = new HashMap();
	private String[] bannedMailHosts = new String[] { "sharklasers", "spam4", "grr.la", "guerrillamail" };
	static final int[] emptyIntZero = new int[2];

	public WebInterfaceImpl(int port) throws RemoteException {
		super(port);

		try {
			InetAddress uhe = InetAddress.getLocalHost();
			this.hostname = uhe.getHostName();
			logger.info("Hostname of local machine used to send registration emails: " + this.hostname);
		} catch (UnknownHostException var3) {
			throw new RemoteException("Could not find localhost for WebInterface", var3);
		}
	}

	public WebInterfaceImpl() throws RemoteException {
	}

	private String getRemoteClientDetails() {
		try {
			return getClientHost();
		} catch (ServerNotActiveException var2) {
			logger.log(Level.WARNING, "Could not get ClientHost details due to " + var2.getMessage(), var2);
			return "Unknown Remote Client";
		}
	}

	public int getPower(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPower for playerID: " + aPlayerID);
		}

		try {
			PlayerInfo iox = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
			iox.load();
			return iox.getPower();
		} catch (IOException var5) {
			logger.log(Level.WARNING, aPlayerID + ": " + var5.getMessage(), var5);
			return 0;
		} catch (NoSuchPlayerException var6) {
			return 0;
		}
	}

	public boolean isRunning(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " isRunning");
		}

		return true;
	}

	public int getPlayerCount(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayerCount");
		}

		return Players.getInstance().numberOfPlayers();
	}

	public int getPremiumPlayerCount(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPremiumPlayerCount");
		}

		return Players.getInstance().numberOfPremiumPlayers();
	}

	public String getTestMessage(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getTestMessage");
		}

		Object var2 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			return "HEj! " + System.currentTimeMillis();
		}
	}

	public void broadcastMessage(String intraServerPassword, String message) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " broadcastMessage: " + message);
		}

		Object var3 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			Server.getInstance().broadCastAlert(message);
		}
	}

	public long getAccountStatusForPlayer(String intraServerPassword, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getAccountStatusForPlayer for player: " + playerName);
		}

		Object var3 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			if (Servers.localServer.id != Servers.loginServer.id) {
				throw new RemoteException("Not a valid request for this server. Ask the login server instead.");
			} else {
				PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

				long var10000;
				try {
					p.load();
					var10000 = p.money;
				} catch (IOException var6) {
					logger.log(Level.WARNING, playerName + ": " + var6.getMessage(), var6);
					return 0L;
				}

				return var10000;
			}
		}
	}

	public Map getBattleRanks(String intraServerPassword, int numberOfRanksToGet) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getBattleRanks number of Ranks: " + numberOfRanksToGet);
		}

		Object var3 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			return Players.getBattleRanks(numberOfRanksToGet);
		}
	}

	public String getServerStatus(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getServerStatus");
		}

		Object var2 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			String toReturn = "Up and running.";
			if (Server.getMillisToShutDown() > -1000L) {
				toReturn = "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds: "
						+ Server.getShutdownReason();
			} else if (Constants.maintaining) {
				toReturn = "The server is in maintenance mode and not open for connections.";
			}

			return toReturn;
		}
	}

	public Map getFriends(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getFriends for playerid: " + aPlayerID);
		}

		Object var4 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			return Players.getFriends(aPlayerID);
		}
	}

	public Map getInventory(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getInventory for playerid: " + aPlayerID);
		}

		Object var4 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();

			try {
				Player p = Players.getInstance().getPlayer(aPlayerID);
				Item inventory = p.getInventory();
				Item[] items = inventory.getAllItems(false);

				for (int x = 0; x < items.length; ++x) {
					toReturn.put(String.valueOf(items[x].getWurmId()), items[x].getName() + ", QL: "
							+ items[x].getQualityLevel() + ", DAM: " + items[x].getDamage());
				}
			} catch (NoSuchPlayerException var10) {
				;
			}

			return toReturn;
		}
	}

	public Map getBodyItems(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getBodyItems for playerid: " + aPlayerID);
		}

		Object var4 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();

			try {
				Player p = Players.getInstance().getPlayer(aPlayerID);
				Body lBody = p.getBody();
				if (lBody != null) {
					Item[] items = lBody.getAllItems();

					for (int x = 0; x < items.length; ++x) {
						toReturn.put(Long.valueOf(items[x].getWurmId()), Long.valueOf(items[x].getParentId()));
					}
				}
			} catch (NoSuchPlayerException var10) {
				;
			}

			return toReturn;
		}
	}

	public Map getSkills(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getSkills for playerid: " + aPlayerID);
		}

		Object var4 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			Skills skills = SkillsFactory.createSkills(aPlayerID);

			try {
				skills.load();
				Skill[] iox = skills.getSkills();

				for (int x = 0; x < iox.length; ++x) {
					toReturn.put(iox[x].getName(), new Float(iox[x].getKnowledge(0.0D)));
				}
			} catch (Exception var9) {
				logger.log(Level.WARNING, aPlayerID + ": " + var9.getMessage(), var9);
			}

			return toReturn;
		}
	}

	public Map getPlayerSummary(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayerSummary for playerid: " + aPlayerID);
		}

		Object var4 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			if (WurmId.getType(aPlayerID) == 0) {
				try {
					Player nsp = Players.getInstance().getPlayer(aPlayerID);
					toReturn.put("Name", nsp.getName());
					if (nsp.citizenVillage != null) {
						Citizen nsp21 = nsp.citizenVillage.getCitizen(aPlayerID);
						toReturn.put("CitizenVillage", nsp.citizenVillage.getName());
						toReturn.put("CitizenRole", nsp21.getRole().getName());
					}

					String nsp22 = "unknown";
					if (nsp.currentVillage != null) {
						nsp22 = nsp.currentVillage.getName() + ", in "
								+ Kingdoms.getNameFor(nsp.currentVillage.kingdom);
					} else if (nsp.currentKingdom != 0) {
						nsp22 = Kingdoms.getNameFor(nsp.currentKingdom);
					}

					toReturn.put("Location", nsp22);
					if (nsp.getDeity() != null) {
						toReturn.put("Deity", nsp.getDeity().name);
					}

					toReturn.put("Faith", new Float(nsp.getFaith()));
					toReturn.put("Favor", new Float(nsp.getFavor()));
					toReturn.put("Gender", Byte.valueOf(nsp.getSex()));
					toReturn.put("Alignment", new Float(nsp.getAlignment()));
					toReturn.put("Kingdom", Byte.valueOf(nsp.getKingdomId()));
					toReturn.put("Battle rank", Integer.valueOf(nsp.getRank()));
					toReturn.put("WurmId", new Long(aPlayerID));
					toReturn.put("Banned", Boolean.valueOf(nsp.getSaveFile().isBanned()));
					toReturn.put("Money in bank", Long.valueOf(nsp.getMoney()));
					toReturn.put("Payment", new Date(nsp.getPaymentExpire()));
					toReturn.put("Email", nsp.getSaveFile().emailAddress);
					toReturn.put("Current server", Integer.valueOf(Servers.localServer.id));
					toReturn.put("Last login", new Date(nsp.getLastLogin()));
					toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
					if (nsp.getSaveFile().isBanned()) {
						toReturn.put("Ban reason", nsp.getSaveFile().banreason);
						toReturn.put("Ban expires in",
								Server.getTimeFor(nsp.getSaveFile().banexpiry - System.currentTimeMillis()));
					}

					toReturn.put("Warnings", String.valueOf(nsp.getSaveFile().getWarnings()));
					if (nsp.isMute()) {
						toReturn.put("Muted", Boolean.TRUE);
						toReturn.put("Mute reason", nsp.getSaveFile().mutereason);
						toReturn.put("Mute expires in",
								Server.getTimeFor(nsp.getSaveFile().muteexpiry - System.currentTimeMillis()));
					}

					toReturn.put("PlayingTime", Server.getTimeFor(nsp.getSaveFile().playingTime));
					toReturn.put("Reputation", Integer.valueOf(nsp.getReputation()));
					if (nsp.getTitle() != null) {
						toReturn.put("Title", nsp.getTitle().getName(nsp.isNotFemale()));
					}

					toReturn.put("Coord x", Integer.valueOf((int) nsp.getStatus().getPositionX() >> 2));
					toReturn.put("Coord y", Integer.valueOf((int) nsp.getStatus().getPositionY() >> 2));
					if (nsp.isPriest()) {
						toReturn.put("Priest", Boolean.TRUE);
					}

					toReturn.put("LoggedOut", Boolean.valueOf(nsp.loggedout));
				} catch (NoSuchPlayerException var10) {
					try {
						PlayerInfo nsp2 = PlayerInfoFactory
								.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
						nsp2.load();
						toReturn.put("Name", nsp2.getName());
						if (nsp2.getDeity() != null) {
							toReturn.put("Deity", nsp2.getDeity().name);
						}

						toReturn.put("Faith", new Float(nsp2.getFaith()));
						toReturn.put("Favor", new Float(nsp2.getFavor()));
						toReturn.put("Current server", Integer.valueOf(nsp2.currentServer));
						toReturn.put("Alignment", new Float(nsp2.getAlignment()));
						toReturn.put("Battle rank", Integer.valueOf(nsp2.getRank()));
						toReturn.put("WurmId", new Long(aPlayerID));
						toReturn.put("Banned", Boolean.valueOf(nsp2.isBanned()));
						toReturn.put("Money in bank", new Long(nsp2.money));
						toReturn.put("Payment", new Date(nsp2.getPaymentExpire()));
						toReturn.put("Email", nsp2.emailAddress);
						toReturn.put("Last login", new Date(nsp2.getLastLogin()));
						toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
						if (nsp2.isBanned()) {
							toReturn.put("Ban reason", nsp2.banreason);
							toReturn.put("Ban expires in",
									Server.getTimeFor(nsp2.banexpiry - System.currentTimeMillis()));
						}

						toReturn.put("Warnings", String.valueOf(nsp2.getWarnings()));
						if (nsp2.isMute()) {
							toReturn.put("Muted", Boolean.TRUE);
							toReturn.put("Mute reason", nsp2.mutereason);
							toReturn.put("Mute expires in",
									Server.getTimeFor(nsp2.muteexpiry - System.currentTimeMillis()));
						}

						toReturn.put("PlayingTime", Server.getTimeFor(nsp2.playingTime));
						toReturn.put("Reputation", Integer.valueOf(nsp2.getReputation()));
						if (nsp2.title != null && nsp2.title.getName(true) != null) {
							toReturn.put("Title", nsp2.title.getName(true));
						}

						if (nsp2.isPriest) {
							toReturn.put("Priest", Boolean.TRUE);
						}
					} catch (IOException var8) {
						logger.log(Level.WARNING, aPlayerID + ":" + var8.getMessage(), var8);
					} catch (NoSuchPlayerException var9) {
						logger.log(Level.WARNING, aPlayerID + ":" + var9.getMessage(), var9);
					}
				}
			} else {
				toReturn.put("Not a player", String.valueOf(aPlayerID));
			}

			return toReturn;
		}
	}

	public long getLocalCreationTime(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getLocalCreationTime");
		}

		return Server.getStartTime();
	}

	public Map getKingdoms(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getKingdoms");
		}

		Object var2 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			if (Servers.localServer.HOMESERVER) {
				toReturn.put(Integer.valueOf(Servers.localServer.KINGDOM),
						Kingdoms.getNameFor(Servers.localServer.KINGDOM));
			} else {
				toReturn.put(Integer.valueOf(1), Kingdoms.getNameFor((byte) 1));
				toReturn.put(Integer.valueOf(3), Kingdoms.getNameFor((byte) 3));
				toReturn.put(Integer.valueOf(2), Kingdoms.getNameFor((byte) 2));
			}

			return toReturn;
		}
	}

	public Map getPlayersForKingdom(String intraServerPassword, int aKingdom) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayersForKingdom: " + aKingdom);
		}

		Object var3 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			Player[] players = Players.getInstance().getPlayers();

			for (int x = 0; x < players.length; ++x) {
				if (players[x].getKingdomId() == aKingdom) {
					toReturn.put(new Long(players[x].getWurmId()), players[x].getName());
				}
			}

			return toReturn;
		}
	}

	public long getPlayerId(String intraServerPassword, String name) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayerId for player name: " + name);
		}

		Object var3 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			return Players.getInstance().getWurmIdByPlayerName(LoginHandler.raiseFirstLetter(name));
		}
	}

	public Map createPlayer(String intraServerPassword, String name, String password, String challengePhrase,
			String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender)
					throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " createPlayer for player name: " + name);
		}

		appearance = (long) Server.rand.nextInt(5);
		this.faceRandom.setSeed(8263186381637L + appearance);
		appearance = this.faceRandom.nextLong();
		HashMap toReturn = new HashMap();
		logger.log(Level.INFO, "Trying to create player " + name);
		Object var13 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			if (isEmailValid(emailAddress)) {
				try {
					toReturn.put("PlayerId", new Long(LoginHandler.createPlayer(name, password, challengePhrase,
							challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
				} catch (Exception var15) {
					toReturn.put("PlayerId", Long.valueOf(-1L));
					toReturn.put("error", var15.getMessage());
					logger.log(Level.WARNING, name + ":" + var15.getMessage(), var15);
				}
			} else {
				toReturn.put("error", "The email address " + emailAddress + " is not valid.");
			}

			return toReturn;
		}
	}

	public Map getPendingAccounts(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPendingAccounts");
		}

		HashMap toReturn = new HashMap();
		Iterator it = PendingAccount.accounts.entrySet().iterator();

		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			toReturn.put((String) entry.getKey(), ((PendingAccount) entry.getValue()).emailAddress + ", "
					+ GeneralUtilities.toGMTString(((PendingAccount) entry.getValue()).expiration));
		}

		return toReturn;
	}

	public Map createPlayerPhaseOne(String intraServerPassword, String aPlayerName, String aEmailAddress)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("error", "The server is currently in maintenance mode.");
			return toReturn;
		} else {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase one " + aPlayerName);
			Object var5 = Server.SYNC_LOCK;
			synchronized (Server.SYNC_LOCK) {
				aPlayerName = LoginHandler.raiseFirstLetter(aPlayerName);
				String errstat = LoginHandler.checkName2(aPlayerName);
				if (errstat.length() == 0) {
					if (PlayerInfoFactory.doesPlayerExist(aPlayerName)) {
						toReturn.put("error", "The name " + aPlayerName + " is taken.");
						return toReturn;
					}

					if (PendingAccount.doesPlayerExist(aPlayerName)) {
						toReturn.put("error", "The name " + aPlayerName + " is reserved for up to two days.");
						return toReturn;
					}

					if (!isEmailValid(aEmailAddress)) {
						toReturn.put("error", "The email " + aEmailAddress + " is invalid.");
						return toReturn;
					}

					String[] numAccounts = PlayerInfoFactory.getAccountsForEmail(aEmailAddress);
					if (numAccounts.length >= 5) {
						String var16 = "";

						for (int var18 = 0; var18 < numAccounts.length; ++var18) {
							var16 = var16 + " " + numAccounts[var18];
						}

						toReturn.put("error",
								"You may only have 5 accounts. Please play Wurm with any of the following:" + var16
										+ ".");
						return toReturn;
					}

					String[] numAccounts2 = PendingAccount.getAccountsForEmail(aEmailAddress);
					String password;
					int expireTime;
					if (numAccounts2.length >= 5) {
						password = "";

						for (expireTime = 0; expireTime < numAccounts2.length; ++expireTime) {
							password = password + " " + numAccounts2[expireTime];
						}

						toReturn.put("error",
								"You may only have 5 accounts. The following accounts are awaiting confirmation by following the link in the verification email:"
										+ password + ".");
						return toReturn;
					}

					String[] pedd = this.bannedMailHosts;
					int var11 = this.bannedMailHosts.length;

					String ex;
					for (expireTime = 0; expireTime < var11; ++expireTime) {
						password = pedd[expireTime];
						if (aEmailAddress.toLowerCase().contains(password)) {
							ex = aEmailAddress.substring(aEmailAddress.indexOf("@"), aEmailAddress.length());
							toReturn.put("error", "We do not accept email addresses from :" + ex + ".");
							return toReturn;
						}
					}

					if (numAccounts.length + numAccounts2.length >= 5) {
						password = "";

						for (expireTime = 0; expireTime < numAccounts.length; ++expireTime) {
							password = password + " " + numAccounts[expireTime];
						}

						for (expireTime = 0; expireTime < numAccounts2.length; ++expireTime) {
							password = password + " " + numAccounts2[expireTime];
						}

						toReturn.put("error",
								"You may only have 5 accounts. The following accounts are already registered or awaiting confirmation by following the link in the verification email:"
										+ password + ".");
						return toReturn;
					}

					password = generateRandomPassword();
					long var17 = System.currentTimeMillis() + 172800000L;
					PendingAccount var19 = new PendingAccount();
					var19.accountName = aPlayerName;
					var19.emailAddress = aEmailAddress;
					var19.expiration = var17;
					var19.password = password;
					if (var19.create()) {
						try {
							if (!Constants.devmode) {
								ex = Mailer.getPhaseOneMail();
								ex = ex.replace("@pname", aPlayerName);
								ex = ex.replace("@email", URLEncoder.encode(aEmailAddress, "UTF-8"));
								ex = ex.replace("@expiration", GeneralUtilities.toGMTString(var17));
								ex = ex.replace("@password", password);
								Mailer.sendMail(mailAccount, aEmailAddress, "Wurm Online character creation request",
										ex);
							} else {
								toReturn.put("Hash", password);
								logger.log(Level.WARNING, "NO MAIL SENT: DEVMODE ACTIVE");
							}

							toReturn.put("ok", "An email has been sent to " + aEmailAddress
									+ ". You will have to click a link in order to proceed with the registration.");
						} catch (Exception var14) {
							toReturn.put("error", "An error occured when sending the mail: " + var14.getMessage()
									+ ". No account was reserved.");
							var19.delete();
							logger.log(Level.WARNING, aEmailAddress + ":" + var14.getMessage(), var14);
						}
					} else {
						toReturn.put("error", "The account could not be created. Please try later.");
						logger.warning(aEmailAddress + " The account could not be created. Please try later.");
					}
				} else {
					toReturn.put("error", errstat);
				}

				return toReturn;
			}
		}
	}

	public Map createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword,
			String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power,
			long appearance, byte gender, String phaseOneHash) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " createPlayerPhaseTwo for player name: " + playerName);
		}

		appearance = (long) Server.rand.nextInt(5);
		this.faceRandom.setSeed(8263186381637L + appearance);
		appearance = this.faceRandom.nextLong();
		return this.createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase,
				challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, 1);
	}

	public Map createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword,
			String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power,
			long appearance, byte gender, String phaseOneHash, int serverId) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		appearance = (long) Server.rand.nextInt(5);
		this.faceRandom.setSeed(8263186381637L + appearance);
		appearance = this.faceRandom.nextLong();
		return this.createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase,
				challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, serverId, true);
	}

	public Map createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword,
			String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power,
			long appearance, byte gender, String phaseOneHash, int serverId, boolean optInEmail)
					throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		byte serverId1 = 1;
		appearance = (long) Server.rand.nextInt(5);
		this.faceRandom.setSeed(8263186381637L + appearance);
		appearance = this.faceRandom.nextLong();
		byte kingdom1 = 4;
		if (kingdom1 == 3) {
			serverId1 = 3;
		}

		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("error", "The server is currently in maintenance mode.");
			return toReturn;
		} else {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase two " + playerName);
			Object var16 = Server.SYNC_LOCK;
			synchronized (Server.SYNC_LOCK) {
				if (playerName != null && hashedIngamePassword != null && challengePhrase != null
						&& challengeAnswer != null && emailAddress != null && phaseOneHash != null) {
					if (challengePhrase.equals(challengeAnswer)) {
						toReturn.put("error",
								"We don\'t allow the password retrieval question and answer to be the same.");
						return toReturn;
					} else {
						playerName = LoginHandler.raiseFirstLetter(playerName);
						String errstat = LoginHandler.checkName2(playerName);
						if (errstat.length() > 0) {
							toReturn.put("error", errstat);
							return toReturn;
						} else if (PlayerInfoFactory.doesPlayerExist(playerName)) {
							toReturn.put("error",
									"The name " + playerName + " is taken. Your reservation must have expired.");
							return toReturn;
						} else if (hashedIngamePassword.length() >= 6 && hashedIngamePassword.length() <= 40) {
							if (challengePhrase.length() >= 4 && challengePhrase.length() <= 120) {
								if (challengeAnswer.length() >= 1 && challengeAnswer.length() <= 20) {
									if (emailAddress.length() > 125) {
										toReturn.put("error", "The email address consists of too many characters.");
										return toReturn;
									} else if (!isEmailValid(emailAddress)) {
										toReturn.put("error", "The email address " + emailAddress + " is not valid.");
										return toReturn;
									} else {
										HashMap var10000;
										try {
											PendingAccount ex = PendingAccount.getAccount(playerName);
											if (ex == null) {
												toReturn.put("PlayerId", Long.valueOf(-1L));
												toReturn.put("error",
														"The verification is done too late or the name was never reserved. The name reservation expires after two days. Please try to create the player again.");
												var10000 = toReturn;
												return var10000;
											}

											if (!ex.password.equals(phaseOneHash)) {
												toReturn.put("PlayerId", Long.valueOf(-1L));
												toReturn.put("error", "The verification hash does not match.");
												return toReturn;
											}

											if (ex.emailAddress.toLowerCase().equals(emailAddress.toLowerCase())) {
												try {
													if (serverId1 == Servers.localServer.id) {
														toReturn.put("PlayerId",
																new Long(LoginHandler.createPlayer(playerName,
																		hashedIngamePassword, challengePhrase,
																		challengeAnswer, emailAddress, kingdom1, power,
																		appearance, gender)));
													} else if (Servers.localServer.LOGINSERVER) {
														ServerEntry cex2 = Servers.getServerWithId(serverId1);
														if (cex2 != null) {
															int tilex = cex2.SPAWNPOINTJENNX;
															int tiley = cex2.SPAWNPOINTJENNY;
															if (kingdom1 == 2) {
																tilex = cex2.SPAWNPOINTMOLX;
																tiley = cex2.SPAWNPOINTMOLY;
															}

															if (kingdom1 == 3) {
																tilex = cex2.SPAWNPOINTLIBX;
																tiley = cex2.SPAWNPOINTLIBY;
															}

															LoginServerWebConnection lsw = new LoginServerWebConnection(
																	serverId1);
															byte[] playerData = lsw.createAndReturnPlayer(playerName,
																	hashedIngamePassword, challengePhrase,
																	challengeAnswer, emailAddress, kingdom1, power,
																	appearance, gender, false, false, false);
															long wurmId = IntraServerConnection.savePlayerToDisk(
																	playerData, tilex, tiley, true, true);
															toReturn.put("PlayerId", Long.valueOf(wurmId));
														} else {
															toReturn.put("PlayerId", Long.valueOf(-1L));
															toReturn.put("error",
																	"Failed to create player " + playerName
																			+ ": The desired server does not exist.");
														}
													} else {
														toReturn.put("PlayerId", Long.valueOf(-1L));
														toReturn.put("error", "Failed to create player " + playerName
																+ ": This is not a login server.");
													}
												} catch (Exception var27) {
													logger.log(Level.WARNING, "Failed to create player " + playerName
															+ "!" + var27.getMessage(), var27);
													toReturn.put("PlayerId", Long.valueOf(-1L));
													toReturn.put("error", "Failed to create player " + playerName + ":"
															+ var27.getMessage());
													var10000 = toReturn;
													return var10000;
												}

												ex.delete();

												try {
													if (!Constants.devmode) {
														String cex21 = Mailer.getPhaseTwoMail();
														cex21 = cex21.replace("@pname", playerName);
														Mailer.sendMail(mailAccount, emailAddress,
																"Wurm Online character creation success", cex21);
														return toReturn;
													}
												} catch (Exception var26) {
													logger.log(Level.WARNING, "Failed to send email to " + emailAddress
															+ " for player " + playerName + ":" + var26.getMessage(),
															var26);
													toReturn.put("error", "Failed to send email to " + emailAddress
															+ " for player " + playerName + ":" + var26.getMessage());
												}

												return toReturn;
											}

											toReturn.put("PlayerId", Long.valueOf(-1L));
											toReturn.put("error",
													"The email supplied does not match with the one that was registered with the name.");
											var10000 = toReturn;
										} catch (Exception var28) {
											logger.log(Level.WARNING,
													"Failed to create player " + playerName + "!" + var28.getMessage(),
													var28);
											toReturn.put("PlayerId", Long.valueOf(-1L));
											toReturn.put("error", var28.getMessage());
											return toReturn;
										}

										return var10000;
									}
								} else {
									toReturn.put("error",
											"The challenge answer must contain at least 1 character and max 20 characters.");
									return toReturn;
								}
							} else {
								toReturn.put("error",
										"The challenge phrase must contain at least 4 characters and max 120 characters.");
								return toReturn;
							}
						} else {
							toReturn.put("error",
									"The hashed password must contain at least 6 characters and maximum 40 characters.");
							return toReturn;
						}
					}
				} else {
					if (playerName == null) {
						toReturn.put("error", "PlayerName is null.");
					}

					if (hashedIngamePassword == null) {
						toReturn.put("error", "hashedIngamePassword is null.");
					}

					if (challengePhrase == null) {
						toReturn.put("error", "ChallengePhrase is null.");
					}

					if (challengeAnswer == null) {
						toReturn.put("error", "ChallengeAnswer is null.");
					}

					if (emailAddress == null) {
						toReturn.put("error", "EmailAddress is null.");
					}

					if (phaseOneHash == null) {
						toReturn.put("error", "phaseOneHash is null.");
					}

					return toReturn;
				}
			}
		}
	}

	public byte[] createAndReturnPlayer(String intraServerPassword, String playerName, String hashedIngamePassword,
			String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power,
			long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed)
					throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (Constants.maintaining) {
			throw new RemoteException("The server is currently in maintenance mode.");
		} else {
			try {
				appearance = (long) Server.rand.nextInt(5);
				this.faceRandom.setSeed(8263186381637L + appearance);
				appearance = this.faceRandom.nextLong();
				logger.log(Level.INFO, getClientHost() + " Received create attempt for " + playerName);
				return LoginHandler.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase,
						challengeAnswer, emailAddress, kingdom, power, appearance, gender, titleKeeper, addPremium,
						passwordIsHashed);
			} catch (Exception var16) {
				logger.log(Level.WARNING, var16.getMessage(), var16);
				throw new RemoteException(var16.getMessage());
			}
		}
	}

	public Map addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		byte executor = 6;
		boolean ok = true;
		String campaignId = "";
		name = LoginHandler.raiseFirstLetter(name);
		HashMap toReturn = new HashMap();
		if (name != null && name.length() != 0) {
			if (moneyToAdd <= 0L) {
				toReturn.put("error", "Invalid amount; must be greater than zero");
				return toReturn;
			} else {
				Object var10 = Server.SYNC_LOCK;
				synchronized (Server.SYNC_LOCK) {
					Change current;
					try {
						Player ex = Players.getInstance().getPlayer(name);
						ex.addMoney(moneyToAdd);
						long iox1 = ex.getMoney();
						new MoneyTransfer(ex.getName(), ex.getWurmId(), iox1, moneyToAdd, transactionDetail, executor,
								campaignId);
						current = new Change(moneyToAdd);
						Change current1 = new Change(iox1);
						ex.save();
						toReturn.put("ok",
								"An amount of " + current.getChangeString()
										+ " has been added to the account. Current balance is "
										+ current1.getChangeString() + ".");
					} catch (NoSuchPlayerException var17) {
						try {
							PlayerInfo iox = PlayerInfoFactory.createPlayerInfo(name);
							iox.load();
							if (iox.wurmId > 0L) {
								iox.setMoney(iox.money + moneyToAdd);
								Change change = new Change(moneyToAdd);
								current = new Change(iox.money);
								iox.save();
								toReturn.put("ok", "An amount of " + change.getChangeString()
										+ " has been added to the account. Current balance is "
										+ current.getChangeString() + ". It may take a while to reach your server.");
								if (Servers.localServer.id != iox.currentServer) {
									new MoneyTransfer(name, iox.wurmId, iox.money, moneyToAdd, transactionDetail,
											executor, campaignId, false);
								} else {
									new MoneyTransfer(iox.getName(), iox.wurmId, iox.money, moneyToAdd,
											transactionDetail, executor, campaignId);
								}
							} else {
								toReturn.put("error", "No player found with the name " + name + ".");
							}
						} catch (IOException var16) {
							logger.log(Level.WARNING, name + ":" + var16.getMessage(), var16);
							throw new RemoteException("An error occured. Please contact customer support.");
						}
					} catch (IOException var18) {
						logger.log(Level.WARNING, name + ":" + var18.getMessage(), var18);
						throw new RemoteException("An error occured. Please contact customer support.");
					} catch (Exception var19) {
						logger.log(Level.WARNING, name + ":" + var19.getMessage(), var19);
						throw new RemoteException("An error occured. Please contact customer support.");
					}

					return toReturn;
				}
			}
		} else {
			toReturn.put("error", "Illegal name.");
			return toReturn;
		}
	}

	public long getMoney(String intraServerPassword, long playerId, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
		if (p == null) {
			p = PlayerInfoFactory.createPlayerInfo(playerName);

			try {
				p.load();
			} catch (IOException var7) {
				logger.log(Level.WARNING, "Failed to load pinfo for " + playerName);
			}

			if (p.wurmId <= 0L) {
				return 0L;
			}
		}

		return p != null ? p.money : 0L;
	}

	public Map reversePayment(String intraServerPassword, long moneyToRemove, int monthsToRemove, int daysToRemove,
			String reversalTransactionID, String originalTransactionID, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		logger.log(Level.INFO,
				this.getRemoteClientDetails() + " Reverse payment for player name: " + playerName
						+ ", reversalTransactionID: " + reversalTransactionID + ", originalTransactionID: "
						+ originalTransactionID);

		try {
			PlayerInfo iox = PlayerInfoFactory.createPlayerInfo(playerName);
			iox.load();
			if (iox.wurmId > 0L) {
				if (moneyToRemove > 0L) {
					Change timeToRemove;
					if (iox.money < moneyToRemove) {
						timeToRemove = new Change(moneyToRemove - iox.money);
						toReturn.put("moneylack", "An amount of " + timeToRemove.getChangeString()
								+ " was lacking from the account. Removing what we can.");
					}

					iox.setMoney(Math.max(0L, iox.money - moneyToRemove));
					timeToRemove = new Change(moneyToRemove);
					Change current = new Change(iox.money);
					iox.save();
					toReturn.put("moneyok",
							"An amount of " + timeToRemove.getChangeString()
									+ " has been removed from the account. Current balance is "
									+ current.getChangeString() + ".");
					if (Servers.localServer.id != iox.currentServer) {
						new MoneyTransfer(playerName, iox.wurmId, iox.money, moneyToRemove, originalTransactionID,
								(byte) 4, "", false);
					} else {
						new MoneyTransfer(playerName, iox.wurmId, iox.money, moneyToRemove, originalTransactionID,
								(byte) 4, "");
					}
				}

				if (daysToRemove > 0 || monthsToRemove > 0) {
					long timeToRemove1 = 0L;
					if (daysToRemove > 0) {
						timeToRemove1 = (long) daysToRemove * 86400000L;
					}

					if (monthsToRemove > 0) {
						timeToRemove1 += (long) monthsToRemove * 86400000L * 30L;
					}

					long currTime = iox.getPaymentExpire();
					currTime = Math.max(currTime, System.currentTimeMillis());
					currTime = Math.max(currTime - timeToRemove1, System.currentTimeMillis());

					try {
						iox.setPaymentExpire(currTime);
						String iox1 = "The premier playing time has expired now.";
						if (System.currentTimeMillis() < currTime) {
							iox1 = "The player now has premier playing time until "
									+ GeneralUtilities.toGMTString(currTime)
									+ ". Your in game player account will be updated shortly.";
						}

						iox.save();
						toReturn.put("timeok", iox1);
						if (iox.currentServer != Servers.localServer.id) {
							new TimeTransfer(playerName, iox.wurmId, -monthsToRemove, false, -daysToRemove,
									originalTransactionID, false);
						} else {
							new TimeTransfer(iox.getName(), iox.wurmId, -monthsToRemove, false, -daysToRemove,
									originalTransactionID);
						}
					} catch (IOException var16) {
						toReturn.put("timeerror",
								iox.getName() + ": failed to set expire to " + currTime + ", " + var16.getMessage());
						logger.log(Level.WARNING,
								iox.getName() + ": failed to set expire to " + currTime + ", " + var16.getMessage(),
								var16);
					}
				}
			} else {
				toReturn.put("error", "No player found with the name " + playerName + ".");
			}

			return toReturn;
		} catch (IOException var17) {
			logger.log(Level.WARNING, playerName + ":" + var17.getMessage(), var17);
			throw new RemoteException("An error occured. Please contact customer support.");
		}
	}

	public Map addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail,
			boolean ingame) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " addMoneyToBank for player name: " + name);
		}

		return this.addMoneyToBank(intraServerPassword, name, -1L, moneyToAdd, transactionDetail, ingame);
	}

	public static String encryptMD5(String plaintext) throws Exception {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException var6) {
			throw new WurmServerException("No such algorithm \'MD5\'", var6);
		}

		try {
			md.update(plaintext.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException var5) {
			throw new WurmServerException("No such encoding: UTF-8", var5);
		}

		byte[] raw = md.digest();
		BigInteger bi = new BigInteger(1, raw);
		String hash = bi.toString(16);
		return hash;
	}

	public Map addMoneyToBank(String intraServerPassword, String name, long wurmId, long moneyToAdd,
			String transactionDetail, boolean ingame) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Object var9 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			if ((name == null || name.length() == 0) && wurmId <= 0L) {
				toReturn.put("error", "Illegal name.");
				return toReturn;
			} else if (moneyToAdd <= 0L) {
				toReturn.put("error", "Invalid amount; must be greater than zero");
				return toReturn;
			} else {
				if (name != null) {
					name = LoginHandler.raiseFirstLetter(name);
				}

				boolean executor = true;
				String campaignId = "";
				logger.log(Level.INFO, this.getRemoteClientDetails() + " Add money to bank 2 , " + moneyToAdd
						+ " for player name: " + name + ", wid " + wurmId);
				if (name != null && name.length() > 0 || wurmId > 0L) {
					Change current;
					try {
						Player ex = null;
						if (wurmId <= 0L) {
							ex = Players.getInstance().getPlayer(name);
						} else {
							ex = Players.getInstance().getPlayer(wurmId);
						}

						ex.addMoney(moneyToAdd);
						long iox1 = ex.getMoney();
						if (!ingame) {
							new MoneyTransfer(ex.getName(), ex.getWurmId(), iox1, moneyToAdd, transactionDetail,
									(byte) 6, "");
						}

						current = new Change(moneyToAdd);
						Change current1 = new Change(iox1);
						ex.save();
						toReturn.put("ok",
								"An amount of " + current.getChangeString()
										+ " has been added to the account. Current balance is "
										+ current1.getChangeString() + ".");
					} catch (NoSuchPlayerException var19) {
						try {
							PlayerInfo iox = null;
							if (name != null && name.length() > 0) {
								iox = PlayerInfoFactory.createPlayerInfo(name);
							} else {
								iox = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
							}

							if (iox != null) {
								iox.load();
								if (iox.wurmId > 0L) {
									iox.setMoney(iox.money + moneyToAdd);
									Change change = new Change(moneyToAdd);
									current = new Change(iox.money);
									iox.save();
									toReturn.put("ok",
											"An amount of " + change.getChangeString()
													+ " has been added to the account. Current balance is "
													+ current.getChangeString()
													+ ". It may take a while to reach your server.");
									if (!ingame) {
										if (Servers.localServer.id != iox.currentServer) {
											new MoneyTransfer(iox.getName(), iox.wurmId, iox.money, moneyToAdd,
													transactionDetail, (byte) 6, "", false);
										} else {
											new MoneyTransfer(iox.getName(), iox.wurmId, iox.money, moneyToAdd,
													transactionDetail, (byte) 6, "");
										}
									}
								} else {
									toReturn.put("error", "No player found with the wurmid " + iox.wurmId + ".");
								}
							} else {
								toReturn.put("error", "No player found with the name " + name + ".");
							}
						} catch (IOException var18) {
							logger.log(Level.WARNING, name + ": " + wurmId + "," + var18.getMessage(), var18);
							throw new RemoteException("An error occured. Please contact customer support.");
						}
					} catch (IOException var20) {
						logger.log(Level.WARNING, name + ":" + wurmId + "," + var20.getMessage(), var20);
						throw new RemoteException("An error occured. Please contact customer support.");
					} catch (Exception var21) {
						logger.log(Level.WARNING, name + ":" + wurmId + "," + var21.getMessage(), var21);
						throw new RemoteException("An error occured. Please contact customer support.");
					}
				}

				return toReturn;
			}
		}
	}

	public long chargeMoney(String intraServerPassword, String playerName, long moneyToCharge) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.log(Level.INFO, this.getRemoteClientDetails() + " ChargeMoney for player name: " + playerName
				+ ", money: " + moneyToCharge);
		if (Servers.localServer.id == Servers.loginServer.id) {
			PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

			try {
				p.load();
				if (p.money > 0L) {
					if (p.money - moneyToCharge < 0L) {
						return -10L;
					} else {
						p.setMoney(p.money - moneyToCharge);
						logger.info(playerName + " was charged " + moneyToCharge + " and now has " + p.money);
						return p.money;
					}
				} else {
					return -10L;
				}
			} catch (IOException var7) {
				logger.log(Level.WARNING, playerName + ": " + var7.getMessage(), var7);
				return -10L;
			}
		} else {
			logger.warning(playerName + " cannot charge " + moneyToCharge + " as this server is not the login server");
			return -10L;
		}
	}

	public Map addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		return this.addPlayingTime(intraServerPassword, name, months, days, transactionDetail, true);
	}

	public Map addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail,
			boolean addSleepPowder) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Object var7 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			HashMap toReturn = new HashMap();
			if (name != null && name.length() != 0 && transactionDetail != null && transactionDetail.length() != 0) {
				if (months >= 0 && days >= 0) {
					boolean ok = true;
					logger.log(Level.INFO, this.getRemoteClientDetails() + " Addplayingtime for player name: " + name
							+ ", months: " + months + ", days: " + days + ", transactionDetail: " + transactionDetail);
					SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd\'-\'hh:mm:ss");
					Object var11 = Server.SYNC_LOCK;
					HashMap var10000;
					synchronized (Server.SYNC_LOCK) {
						long timeToAdd = 0L;
						if (days != 0) {
							timeToAdd = (long) days * 86400000L;
						}

						if (months != 0) {
							timeToAdd += (long) months * 86400000L * 30L;
						}

						try {
							Player ex = Players.getInstance().getPlayer(name);
							long var34 = ex.getPaymentExpire();
							if (timeToAdd > 0L) {
								if (var34 <= 0L) {
									Server.addNewPlayer(ex.getName());
								} else {
									Server.incrementOldPremiums(ex.getName());
								}
							}

							var34 = Math.max(var34, System.currentTimeMillis());
							var34 += timeToAdd;

							try {
								ex.setPaymentExpire(var34);
								new TimeTransfer(ex.getName(), ex.getWurmId(), months, addSleepPowder, days,
										transactionDetail);
							} catch (IOException var25) {
								logger.log(Level.WARNING,
										ex.getName() + ": failed to set expire to " + var34 + ", " + var25.getMessage(),
										var25);
							}

							String expireString = "You now have premier playing time until "
									+ formatter.format(new Date(var34)) + ".";
							ex.save();
							toReturn.put("ok", expireString);
							Message var35 = new Message((Creature) null, (byte) 3, ":Event", expireString);
							var35.setReceiver(ex.getWurmId());
							Server.getInstance().addMessage(var35);
							logger.info(ex.getName() + ' ' + expireString);
							if (addSleepPowder) {
								try {
									Item var36 = ex.getInventory();

									for (int var37 = 0; var37 < months; ++var37) {
										Item i = ItemFactory.createItem(666, 99.0F, "");
										var36.insertItem(i, true);
									}

									logger.log(Level.INFO, "Inserted " + months + " sleep powder in " + ex.getName()
											+ " inventory " + var36.getWurmId());
									Message var38 = new Message((Creature) null, (byte) 3, ":Event",
											"You have received " + months + " sleeping powders in your inventory.");
									var38.setReceiver(ex.getWurmId());
									Server.getInstance().addMessage(var38);
								} catch (Exception var26) {
									logger.log(Level.INFO, var26.getMessage(), var26);
								}
							}

							var10000 = toReturn;
							return var10000;
						} catch (NoSuchPlayerException var27) {
							;
						} catch (IOException var28) {
							logger.log(Level.WARNING, name + ":" + var28.getMessage(), var28);
							throw new RemoteException("An error occured. Please contact customer support.");
						} catch (Exception var29) {
							logger.log(Level.WARNING, name + ":" + var29.getMessage(), var29);
							throw new RemoteException("An error occured. Please contact customer support.");
						}

						try {
							PlayerInfo iox = PlayerInfoFactory.createPlayerInfo(name);
							iox.load();
							if (iox.wurmId > 0L) {
								long currTime = iox.getPaymentExpire();
								if (timeToAdd > 0L) {
									if (currTime <= 0L) {
										Server.addNewPlayer(iox.getName());
									} else {
										Server.incrementOldPremiums(iox.getName());
									}
								}

								currTime = Math.max(currTime, System.currentTimeMillis());
								currTime += timeToAdd;

								try {
									iox.setPaymentExpire(currTime);
								} catch (IOException var24) {
									logger.log(Level.WARNING, iox.getName() + ": failed to set expire to " + currTime
											+ ", " + var24.getMessage(), var24);
								}

								ServerEntry entry = Servers.getServerWithId(iox.currentServer);
								String expireString1 = "Your premier playing time has expired now.";
								if (System.currentTimeMillis() < currTime) {
									if (entry.entryServer) {
										expireString1 = "You now have premier playing time until "
												+ formatter.format(new Date(currTime))
												+ ". Your in game player account will be updated shortly. NOTE that you will have to use a portal to get to the premium servers in order to benefit from it.";
									} else {
										expireString1 = "You now have premier playing time until "
												+ formatter.format(new Date(currTime))
												+ ". Your in game player account will be updated shortly.";
									}
								}

								iox.save();
								toReturn.put("ok", expireString1);
								logger.info(iox.getName() + ' ' + expireString1);
								if (iox.currentServer != Servers.localServer.id) {
									new TimeTransfer(name, iox.wurmId, months, addSleepPowder, days, transactionDetail,
											false);
								} else {
									new TimeTransfer(iox.getName(), iox.wurmId, months, addSleepPowder, days,
											transactionDetail);
									if (addSleepPowder) {
										try {
											long ex1 = DbCreatureStatus.getInventoryIdFor(iox.wurmId);

											for (int x = 0; x < months; ++x) {
												Item i1 = ItemFactory.createItem(666, 99.0F, "");
												i1.setParentId(ex1, true);
												i1.setOwnerId(iox.wurmId);
											}

											logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline "
													+ iox.getName() + " inventory " + ex1);
										} catch (Exception var30) {
											logger.log(Level.INFO, var30.getMessage(), var30);
										}
									}
								}

								var10000 = toReturn;
							} else {
								toReturn.put("error", "No player found with the name " + name + ".");
								var10000 = toReturn;
								return var10000;
							}
						} catch (IOException var31) {
							logger.log(Level.WARNING, name + ":" + var31.getMessage(), var31);
							throw new RemoteException("An error occured. Please contact customer support.");
						}
					}

					return var10000;
				} else {
					toReturn.put("error",
							"Illegal arguments. Make sure that the values for days and months are not negative.");
					return toReturn;
				}
			} else {
				toReturn.put("error",
						"Illegal arguments. Check if name or transaction detail is null or empty strings.");
				return toReturn;
			}
		}
	}

	public Map getDeeds(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getDeeds");
		}

		HashMap toReturn = new HashMap();
		Village[] vills = Villages.getVillages();

		for (int x = 0; x < vills.length; ++x) {
			toReturn.put(Integer.valueOf(vills[x].id), vills[x].getName());
		}

		return toReturn;
	}

	public Map getDeedSummary(String intraServerPassword, int aVillageID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getDeedSummary for villageID: " + aVillageID);
		}

		try {
			Village ex = Villages.getVillage(aVillageID);
			HashMap toReturn = new HashMap();
			toReturn.put("Villageid", Integer.valueOf(ex.getId()));
			toReturn.put("Deedid", Long.valueOf(ex.getDeedId()));
			toReturn.put("Name", ex.getName());
			toReturn.put("Motto", ex.getMotto());
			toReturn.put("Location", Kingdoms.getNameFor(ex.kingdom));
			toReturn.put("Size", Integer.valueOf((ex.getEndX() - ex.getStartX()) / 2));
			toReturn.put("Founder", ex.getFounderName());
			toReturn.put("Mayor", ex.mayorName);
			if (ex.disband > 0L) {
				toReturn.put("Disbanding in", Server.getTimeFor(ex.disband - System.currentTimeMillis()));
				toReturn.put("Disbander", Players.getInstance().getNameFor(ex.disbander));
			}

			toReturn.put("Citizens", Integer.valueOf(ex.citizens.size()));
			toReturn.put("Allies", Integer.valueOf(ex.getAllies().length));
			if (ex.guards != null) {
				toReturn.put("guards", Integer.valueOf(ex.guards.size()));
			}

			try {
				short[] sp = ex.getTokenCoords();
				toReturn.put("Token Coord x", Integer.valueOf(sp[0]));
				toReturn.put("Token Coord y", Integer.valueOf(sp[1]));
			} catch (NoSuchItemException var6) {
				;
			}

			return toReturn;
		} catch (Exception var7) {
			logger.log(Level.WARNING, var7.getMessage(), var7);
			throw new RemoteException(var7.getMessage());
		}
	}

	public Map getPlayersForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayersForDeed for villageID: " + aVillageID);
		}

		HashMap toReturn = new HashMap();

		try {
			Village ex = Villages.getVillage(aVillageID);
			Citizen[] citizens = ex.getCitizens();

			for (int x = 0; x < citizens.length; ++x) {
				if (WurmId.getType(citizens[x].getId()) == 0) {
					try {
						toReturn.put(Players.getInstance().getNameFor(citizens[x].getId()),
								new Long(citizens[x].getId()));
					} catch (NoSuchPlayerException var8) {
						;
					}
				}
			}

			return toReturn;
		} catch (Exception var9) {
			logger.log(Level.WARNING, var9.getMessage(), var9);
			throw new RemoteException(var9.getMessage());
		}
	}

	public Map getAlliesForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getAlliesForDeed for villageID: " + aVillageID);
		}

		HashMap toReturn = new HashMap();

		try {
			Village ex = Villages.getVillage(aVillageID);
			Village[] allies = ex.getAllies();

			for (int x = 0; x < allies.length; ++x) {
				toReturn.put(allies[x].getName(), Integer.valueOf(allies[x].getId()));
			}

			return toReturn;
		} catch (Exception var7) {
			logger.log(Level.WARNING, var7.getMessage(), var7);
			throw new RemoteException(var7.getMessage());
		}
	}

	public String[] getHistoryForDeed(String intraServerPassword, int villageID, int maxLength) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getHistoryForDeed for villageID: " + villageID
					+ ", maxLength: " + maxLength);
		}

		try {
			Village ex = Villages.getVillage(villageID);
			return ex.getHistoryAsStrings(maxLength);
		} catch (Exception var5) {
			logger.log(Level.WARNING, var5.getMessage(), var5);
			throw new RemoteException(var5.getMessage());
		}
	}

	public String[] getAreaHistory(String intraServerPassword, int maxLength) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getAreaHistory maxLength: " + maxLength);
		}

		return HistoryManager.getHistory(maxLength);
	}

	public Map getItemSummary(String intraServerPassword, long aWurmID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getItemSummary for WurmId: " + aWurmID);
		}

		HashMap toReturn = new HashMap();

		try {
			Item ex = Items.getItem(aWurmID);
			toReturn.put("WurmId", new Long(aWurmID));
			toReturn.put("Name", ex.getName());
			toReturn.put("QL", String.valueOf(ex.getQualityLevel()));
			toReturn.put("DMG", String.valueOf(ex.getDamage()));
			toReturn.put("SizeX", String.valueOf(ex.getSizeX()));
			toReturn.put("SizeY", String.valueOf(ex.getSizeY()));
			toReturn.put("SizeZ", String.valueOf(ex.getSizeZ()));
			if (ex.getOwnerId() != -10L) {
				toReturn.put("Owner", new Long(ex.getOwnerId()));
			} else {
				toReturn.put("Last owner", new Long(ex.lastOwner));
			}

			toReturn.put("Coord x", Integer.valueOf((int) ex.getPosX() >> 2));
			toReturn.put("Coord y", Integer.valueOf((int) ex.getPosY() >> 2));
			toReturn.put("Creator", ex.creator);
			toReturn.put("Creationdate", WurmCalendar.getTimeFor(ex.creationDate));
			toReturn.put("Description", ex.getDescription());
			toReturn.put("Material", Item.getMaterialString(ex.getMaterial()));
			return toReturn;
		} catch (Exception var6) {
			logger.log(Level.WARNING, var6.getMessage(), var6);
			throw new RemoteException(var6.getMessage());
		}
	}

	public Map getPlayerIPAddresses(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayerIPAddresses");
		}

		HashMap toReturn = new HashMap();
		Player[] playerArr = Players.getInstance().getPlayersByIp();

		for (int x = 0; x < playerArr.length; ++x) {
			if (playerArr[x].getSaveFile().getPower() == 0) {
				toReturn.put(playerArr[x].getName(), playerArr[x].getSaveFile().getIpaddress());
			}
		}

		return toReturn;
	}

	public Map getNameBans(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getNameBans");
		}

		HashMap toReturn = new HashMap();
		BannedIp[] bips = Players.getInstance().getPlayersBanned();
		if (bips.length > 0) {
			for (int x = 0; x < bips.length; ++x) {
				long daytime = bips[x].expiry - System.currentTimeMillis();
				toReturn.put(bips[x].ipaddress, Server.getTimeFor(daytime) + ", " + bips[x].reason);
			}
		}

		return toReturn;
	}

	public Map getIPBans(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getIPBans");
		}

		HashMap toReturn = new HashMap();
		BannedIp[] bips = Players.getInstance().getIpsBanned();
		if (bips.length > 0) {
			for (int x = 0; x < bips.length; ++x) {
				long daytime = bips[x].expiry - System.currentTimeMillis();
				toReturn.put(bips[x].ipaddress, Server.getTimeFor(daytime) + ", " + bips[x].reason);
			}
		}

		return toReturn;
	}

	public Map getWarnings(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getWarnings");
		}

		HashMap toReturn = new HashMap();
		toReturn.put("Not implemented", "Need a name to check.");
		return toReturn;
	}

	public String getWurmTime(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getWurmTime");
		}

		return WurmCalendar.getTime();
	}

	public String getUptime(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getUptime");
		}

		return Server.getTimeFor(System.currentTimeMillis() - Server.getStartTime());
	}

	public String getNews(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getNews");
		}

		return NewsInfo.getInfo();
	}

	public String getGameInfo(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getGameInfo");
		}

		return WurmInfo.getInfo() + WurmInfo2.getInfo();
	}

	public Map getKingdomInfluence(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getKingdomInfluence");
		}

		HashMap toReturn = new HashMap();
		Zones.calculateZones(false);
		Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

		for (int x = 0; x < kingdoms.length; ++x) {
			toReturn.put("Percent controlled by " + kingdoms[x].getName(),
					twoDecimals.format((double) Zones.getPercentLandForKingdom(kingdoms[x].getId())));
		}

		return toReturn;
	}

	public Map getMerchantSummary(String intraServerPassword, long aWurmID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getMerchantSummary for WurmID: " + aWurmID);
		}

		HashMap toReturn = new HashMap();
		toReturn.put("Not implemented", "not yet");
		return toReturn;
	}

	public Map getBankAccount(String intraServerPassword, long aPlayerID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getBankAccount for playerid: " + aPlayerID);
		}

		HashMap toReturn = new HashMap();
		logger.log(Level.INFO, "GetBankAccount " + aPlayerID);

		try {
			Bank e = Banks.getBank(aPlayerID);
			if (e != null) {
				toReturn.put("BankID", Long.valueOf(e.id));
				toReturn.put("Owner", Long.valueOf(e.owner));
				toReturn.put("StartedMoving", Long.valueOf(e.startedMoving));
				toReturn.put("Open", Boolean.valueOf(e.open));
				toReturn.put("Size", Integer.valueOf(e.size));

				try {
					Village lTargetVillageID = e.getCurrentVillage();
					if (lTargetVillageID != null) {
						toReturn.put("CurrentVillageID", Integer.valueOf(lTargetVillageID.getId()));
						toReturn.put("CurrentVillageName", lTargetVillageID.getName());
					}
				} catch (BankUnavailableException var11) {
					;
				}

				int var13 = e.targetVillage;
				if (var13 > 0) {
					toReturn.put("TargetVillageID", Integer.valueOf(var13));
				}

				BankSlot[] lSlots = e.slots;
				if (lSlots != null && lSlots.length > 0) {
					HashMap lItemsMap = new HashMap(lSlots.length + 1);

					for (int i = 0; i < lSlots.length; ++i) {
						if (lSlots[i] == null) {
							logger.log(Level.INFO, "Weird. Bank Slot " + i + " is null for " + aPlayerID);
						} else {
							Item lItem = lSlots[i].item;
							if (lItem != null) {
								lItemsMap.put(Long.valueOf(lItem.getWurmId()), lItem.getName() + ", Inserted: "
										+ lSlots[i].inserted + ", Stasis: " + lSlots[i].stasis);
							}
						}
					}

					if (lItemsMap != null && lItemsMap.size() > 0) {
						toReturn.put("Items", lItemsMap);
					}
				}
			} else {
				toReturn.put("Error", "Cannot find bank for player ID " + aPlayerID);
			}
		} catch (RuntimeException var12) {
			logger.log(Level.WARNING, "Error: " + var12.getMessage(), var12);
			toReturn.put("Error", "Problem getting bank account for player ID " + aPlayerID + ", " + var12);
		}

		return toReturn;
	}

	public Map authenticateUser(String intraServerPassword, String playerName, String emailAddress,
			String hashedIngamePassword, Map params) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
		}

		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("ResponseCode0", "NOTOK");
			toReturn.put("ErrorMessage0", "The server is currently unavailable.");
			toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
			return toReturn;
		} else {
			try {
				boolean ew = false;
				Object answer = params.get("VerifiedPayPalAccount");
				if (answer != null && answer instanceof Boolean) {
					ew = ((Boolean) answer).booleanValue();
				}

				boolean rev = false;
				answer = params.get("ChargebackOrReversal");
				if (answer != null && answer instanceof Boolean) {
					rev = ((Boolean) answer).booleanValue();
				}

				Date lastReversal = (Date) params.get("LastChargebackOrReversal");
				Date first = (Date) params.get("FirstTransactionDate");
				Date last = (Date) params.get("LastTransactionDate");
				boolean total = false;
				answer = params.get("TotalEurosSuccessful");
				if (answer != null && answer instanceof Integer) {
					int total1 = ((Integer) answer).intValue();
					if (total1 < 0) {
						total = false;
					}
				}

				int lastMonthEuros = 0;
				answer = params.get("LastMonthEurosSuccessful");
				if (answer != null && answer instanceof Integer) {
					lastMonthEuros = ((Integer) answer).intValue();
					if (lastMonthEuros < 0) {
						lastMonthEuros = 0;
					}
				}

				String ipAddress = (String) params.get("IP");
				if (ipAddress != null) {
					logger.log(Level.INFO, "IP:" + ipAddress);
					Long file = (Long) ipAttempts.get(ipAddress);
					if (file != null && System.currentTimeMillis() - file.longValue() < 5000L) {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "Too many logon attempts. Please try again in a few seconds.");
						toReturn.put("display_text0", "Too many logon attempts. Please try again in a few seconds.");
						return toReturn;
					}

					ipAttempts.put(ipAddress, Long.valueOf(System.currentTimeMillis()));
				}

				PlayerInfo file1 = PlayerInfoFactory.createPlayerInfo(playerName);
				if (file1.undeadType != 0) {
					toReturn.put("ResponseCode0", "NOTOK");
					toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
					toReturn.put("display_text0", "Undeads not allowed in here!");
					return toReturn;
				} else {
					try {
						file1.load();
						if (file1.undeadType != 0) {
							toReturn.put("ResponseCode0", "NOTOK");
							toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
							toReturn.put("display_text0", "Undeads not allowed in here!");
							return toReturn;
						}
					} catch (IOException var19) {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "An error occurred when loading your account.");
						toReturn.put("display_text0", "An error occurred when loading your account.");
						logger.log(Level.WARNING, var19.getMessage(), var19);
						return toReturn;
					}

					if (file1.overRideShop || !rev
							|| lastReversal != null && last != null && !lastReversal.after(last)) {
						Map toReturn1 = this.authenticateUser(intraServerPassword, playerName, emailAddress,
								hashedIngamePassword);
						Integer max = (Integer) toReturn1.get("maximum_silver0");
						if (max != null) {
							int maxval = max.intValue();
							if (file1.overRideShop) {
								maxval = 50 + Math.min(50, (int) (file1.playingTime / 3600000L * 3L));
								toReturn1.put("maximum_silver0", Integer.valueOf(maxval));
							} else if (lastMonthEuros >= 400) {
								byte maxval1 = 0;
								toReturn1.put("maximum_silver0", Integer.valueOf(maxval1));
								toReturn1.put("display_text0", "You may only purchase 400 silver via PayPal per month");
							}
						}

						return toReturn1;
					} else {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "This paypal account has reversed transactions registered.");
						toReturn.put("display_text0", "This paypal account has reversed transactions registered.");
						return toReturn;
					}
				}
			} catch (Exception var20) {
				logger.log(Level.WARNING, "Error: " + var20.getMessage(), var20);
				toReturn.put("ResponseCode0", "NOTOK");
				toReturn.put("ErrorMessage0", "An error occured.");
				return toReturn;
			}
		}
	}

	public Map doesPlayerExist(String intraServerPassword, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " doesPlayerExist for player name: " + playerName);
		}

		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("ResponseCode", "NOTOK");
			toReturn.put("ErrorMessage", "The server is currently unavailable.");
			toReturn.put("display_text", "The server is currently unavailable.");
			return toReturn;
		} else {
			toReturn.put("ResponseCode", "OK");
			if (playerName != null) {
				PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);

				try {
					file.load();
					if (file.wurmId <= 0L) {
						toReturn.clear();
						toReturn.put("ResponseCode", "NOTOK");
						toReturn.put("ErrorMessage", "No such player on the " + Servers.localServer.name
								+ " game server. Maybe it has been deleted due to inactivity.");
						toReturn.put("display_text", "No such player on the " + Servers.localServer.name
								+ " game server. Maybe it has been deleted due to inactivity.");
					}
				} catch (Exception var6) {
					toReturn.clear();
					toReturn.put("ResponseCode", "NOTOK");
					toReturn.put("ErrorMessage", var6.getMessage());
					toReturn.put("display_text", "An error occurred on the " + Servers.localServer.name
							+ " game server: " + var6.getMessage());
				}
			}

			return toReturn;
		}
	}

	public Map authenticateUser(String intraServerPassword, String playerName, String emailAddress,
			String hashedIngamePassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
		}

		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("ResponseCode0", "NOTOK");
			toReturn.put("ErrorMessage0", "The server is currently unavailable.");
			toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
			return toReturn;
		} else {
			int var14;
			if (playerName != null) {
				PlayerInfo infos = PlayerInfoFactory.createPlayerInfo(playerName);
				if (infos.undeadType != 0) {
					toReturn.put("ResponseCode0", "NOTOK");
					toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
					toReturn.put("display_text0", "Undeads not allowed in here!");
					return toReturn;
				}

				try {
					infos.load();
					if (infos.undeadType != 0) {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
						toReturn.put("display_text0", "Undeads not allowed in here!");
						return toReturn;
					}

					if (infos.wurmId <= 0L) {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "No such player.");
					} else if (hashedIngamePassword.equals(infos.getPassword())) {
						if (Servers.isThisLoginServer()) {
							LoginServerWebConnection x = new LoginServerWebConnection(infos.currentServer);
							Map maxMonths = x.doesPlayerExist(playerName);
							String maxMonthsMillis = (String) maxMonths.get("ResponseCode");
							if (maxMonthsMillis != null && maxMonthsMillis.equals("NOTOK")) {
								toReturn.put("ResponseCode0", "NOTOK");
								toReturn.put("ErrorMessage0", maxMonths.get("ErrorMessage"));
								toReturn.put("display_text0", maxMonths.get("display_text"));
								return toReturn;
							}
						}

						toReturn.put("ErrorMessage0", "");
						if (infos.getPaymentExpire() < 0L) {
							toReturn.put("display_text0",
									"You are new to the game and may give away an in-game referral to the person who introduced you to Wurm Online using the chat command \'/refer\' if you purchase premium game time.");
						} else {
							toReturn.put("display_text0",
									"Don\'t forget to use the in-game \'/refer\' chat command to refer the one who introduced you to Wurm Online.");
						}

						if (infos.getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
							toReturn.put("display_text0",
									"You have less than a week left of premium game time so the amount of coins you can purchase is somewhat limited.");
							toReturn.put("maximum_silver0", Integer.valueOf(10));
						} else {
							toReturn.put("maximum_silver0",
									Integer.valueOf(20 + Math.min(100, (int) (infos.playingTime / 3600000L * 3L))));
						}

						if (!infos.overRideShop && infos.isBanned()) {
							toReturn.put("PurchaseOk0", "NOTOK");
							toReturn.put("maximum_silver0", Integer.valueOf(0));
							toReturn.put("display_text0", "You have been banned. Reason: " + infos.banreason);
							toReturn.put("ErrorMessage0", "The player has been banned. Reason: " + infos.banreason);
						} else {
							toReturn.put("PurchaseOk0", "OK");
						}

						boolean var13 = false;
						if (infos.getPaymentExpire() > System.currentTimeMillis()) {
							long var15 = System.currentTimeMillis() + 36288000000L - infos.getPaymentExpire();
							var14 = (int) (var15 / 2419200000L);
							if (var14 < 0) {
								var14 = 0;
							}
						} else {
							var14 = 12;
						}

						toReturn.put("maximum_months0", Integer.valueOf(var14));
						toReturn.put("new_customer0", Boolean.valueOf(infos.getPaymentExpire() <= 0L));
						toReturn.put("ResponseCode0", "OK");
						toReturn.put("PlayerID0", new Long(infos.wurmId));
						toReturn.put("ingameBankBalance0", new Long(infos.money));
						toReturn.put("PlayingTimeExpire0", new Long(infos.getPaymentExpire()));
					} else {
						toReturn.put("ResponseCode0", "NOTOK");
						toReturn.put("ErrorMessage0", "Password does not match.");
					}
				} catch (Exception var11) {
					toReturn.put("ResponseCode0", "NOTOK");
					toReturn.put("ErrorMessage0", var11.getMessage());
					logger.log(Level.WARNING, var11.getMessage(), var11);
				}
			} else if (isEmailValid(emailAddress)) {
				PlayerInfo[] var12 = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);

				for (var14 = 0; var14 < var12.length; ++var14) {
					if (var12[var14].getPassword().equals(hashedIngamePassword)) {
						toReturn.put("ErrorMessage" + var14, "");
						if (var12[var14].getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
							toReturn.put("maximum_silver" + var14, Integer.valueOf(10));
						} else {
							toReturn.put("maximum_silver" + var14,
									Integer.valueOf(10 + Math.min(100, (int) (var12[var14].playingTime / 86400000L))));
						}

						if (!var12[var14].overRideShop && var12[var14].isBanned()) {
							toReturn.put("PurchaseOk" + var14, "NOTOK");
							toReturn.put("maximum_silver" + var14, Integer.valueOf(0));
							toReturn.put("display_text" + var14,
									"You have been banned. Reason: " + var12[var14].banreason);
							toReturn.put("ErrorMessage" + var14,
									"The player has been banned. Reason: " + var12[var14].banreason);
						} else {
							toReturn.put("PurchaseOk" + var14, "OK");
						}

						boolean var16 = false;
						int var17;
						if (var12[var14].getPaymentExpire() > System.currentTimeMillis()) {
							long var18 = System.currentTimeMillis() + 36288000000L - var12[var14].getPaymentExpire();
							var17 = (int) (var18 / 2419200000L);
							if (var17 < 0) {
								var17 = 0;
							}
						} else {
							var17 = 12;
						}

						toReturn.put("maximum_months" + var14, Integer.valueOf(var17));
						toReturn.put("new_customer" + var14, Boolean.valueOf(var12[var14].getPaymentExpire() <= 0L));
						toReturn.put("ResponseCode" + var14, "OK");
						toReturn.put("PlayerID" + var14, new Long(var12[var14].wurmId));
						toReturn.put("ingameBankBalance" + var14, new Long(var12[var14].money));
						toReturn.put("PlayingTimeExpire" + var14, new Long(var12[var14].getPaymentExpire()));
					} else {
						toReturn.put("ResponseCode" + var14, "NOTOK");
						toReturn.put("ErrorMessage" + var14, "Password does not match.");
					}
				}
			} else {
				toReturn.put("ResponseCode0", "NOTOK");
				toReturn.put("ErrorMessage0", "Invalid email: " + emailAddress);
			}

			return toReturn;
		}
	}

	public Map changePassword(String intraServerPassword, String playerName, String emailAddress, String newPassword)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();

		try {
			toReturn.put("Result", "Unknown email.");
			logger.log(Level.INFO,
					this.getRemoteClientDetails() + " Changepassword Name: " + playerName + ", email: " + emailAddress);
			int nums;
			HashMap var13;
			if (emailAddress != null) {
				if (isEmailValid(emailAddress)) {
					PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
					int iox = 0;

					for (nums = 0; nums < p.length; ++nums) {
						if (p[nums].getPower() == 0) {
							try {
								p[nums].updatePassword(newPassword);
								if (p[nums].currentServer != Servers.localServer.id) {
									new PasswordTransfer(p[nums].getName(), p[nums].wurmId, p[nums].getPassword(),
											System.currentTimeMillis(), false);
								}

								++iox;
								toReturn.put("Account" + iox, p[nums].getName() + " password was updated.");
							} catch (IOException var18) {
								logger.log(Level.WARNING, "Failed to update password for " + p[nums].getName(), var18);
								toReturn.put("Error" + iox, p[nums].getName() + " password was _not_ updated.");
							}
						} else {
							toReturn.put("Error" + iox, "Failed to update password for " + p[nums].getName());
							logger.warning("Failed to update password for " + p[nums].getName() + " as power is "
									+ p[nums].getPower());
						}
					}

					if (iox > 0) {
						toReturn.put("Result", iox + " player accounts were affected.");
					} else {
						toReturn.put("Error", iox + " player accounts were affected.");
					}

					var13 = toReturn;
					return var13;
				}

				toReturn.put("Error", emailAddress + " is an invalid email.");
			} else if (playerName != null) {
				PlayerInfo var22 = PlayerInfoFactory.createPlayerInfo(playerName);

				try {
					var22.load();
					if (isEmailValid(var22.emailAddress)) {
						emailAddress = var22.emailAddress;
						PlayerInfo[] var23 = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
						nums = 0;
						boolean failed = false;

						for (int x = 0; x < var23.length; ++x) {
							if (var23[x].getPower() == 0) {
								try {
									var23[x].updatePassword(newPassword);
									if (var23[x].currentServer != Servers.localServer.id) {
										new PasswordTransfer(var23[x].getName(), var23[x].wurmId,
												var23[x].getPassword(), System.currentTimeMillis(), false);
									}

									++nums;
									toReturn.put("Account" + nums, var23[x].getName() + " password was updated.");
								} catch (IOException var19) {
									failed = true;
									toReturn.put("Error" + nums, "Failed to update password for a player.");
								}
							} else {
								failed = true;
								logger.warning("Failed to update password for " + var23[x].getName() + " as power is "
										+ var23[x].getPower());
							}
						}

						if (nums > 0) {
							toReturn.put("Result", nums + " player accounts were affected.");
						} else {
							toReturn.put("Error", nums + " player accounts were affected.");
						}

						if (failed) {
							logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
						}

						var13 = toReturn;
						return var13;
					}

					toReturn.put("Error", emailAddress + " is an invalid email.");
				} catch (IOException var20) {
					toReturn.put("Error", "Failed to load player data. Password not changed.");
					logger.log(Level.WARNING, var20.getMessage(), var20);
				}
			}

			var13 = toReturn;
			return var13;
		} finally {
			logger.info("Changepassword Name: " + playerName + ", email: " + emailAddress + ", exit: " + toReturn);
		}
	}

	public Map changePassword(String intraServerPassword, String playerName, String emailAddress,
			String hashedOldPassword, String newPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		toReturn.put("Result", "Unknown email.");
		logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword 2 for player name: " + playerName);
		boolean iox;
		int nums;
		if (emailAddress != null) {
			if (isEmailValid(emailAddress)) {
				PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
				iox = false;
				int infos = 0;

				for (nums = 0; nums < p.length; ++nums) {
					if (p[nums].getPassword().equals(hashedOldPassword)) {
						iox = true;
					}
				}

				if (iox) {
					boolean var19 = false;

					for (int failed = 0; failed < p.length; ++failed) {
						if (p[failed].getPower() == 0) {
							try {
								p[failed].updatePassword(newPassword);
								if (p[failed].currentServer != Servers.localServer.id) {
									new PasswordTransfer(p[failed].getName(), p[failed].wurmId, p[failed].getPassword(),
											System.currentTimeMillis(), false);
								}

								++infos;
								toReturn.put("Account" + infos, p[failed].getName() + " password was updated.");
							} catch (IOException var14) {
								var19 = true;
								toReturn.put("Error" + infos, "Failed to update password for " + p[failed].getName());
							}
						} else {
							var19 = true;
							toReturn.put("Error" + infos, p[failed].getName() + " password was _not_ updated.");
						}
					}

					if (var19) {
						logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
					}
				}

				if (infos > 0) {
					toReturn.put("Result", infos + " player accounts were affected.");
				} else {
					toReturn.put("Error", infos + " player accounts were affected.");
				}

				return toReturn;
			}

			toReturn.put("Result", emailAddress + " is an invalid email.");
		} else if (playerName != null) {
			PlayerInfo var17 = PlayerInfoFactory.createPlayerInfo(playerName);

			try {
				var17.load();
				iox = false;
				if (isEmailValid(var17.emailAddress)) {
					emailAddress = var17.emailAddress;
					PlayerInfo[] var18 = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);

					for (nums = 0; nums < var18.length; ++nums) {
						if (var18[nums].getPassword().equals(hashedOldPassword)) {
							iox = true;
						}
					}

					nums = 0;
					if (iox) {
						boolean var20 = false;

						for (int x = 0; x < var18.length; ++x) {
							if (var18[x].getPower() == 0) {
								try {
									var18[x].updatePassword(newPassword);
									if (var18[x].currentServer != Servers.localServer.id) {
										new PasswordTransfer(var18[x].getName(), var18[x].wurmId,
												var18[x].getPassword(), System.currentTimeMillis(), false);
									}

									++nums;
									toReturn.put("Account" + nums, var18[x].getName() + " password was updated.");
								} catch (IOException var15) {
									var20 = true;
									toReturn.put("Error" + x, "Failed to update password for " + var18[x].getName());
								}
							} else {
								var20 = true;
							}
						}

						if (var20) {
							logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
						}
					}

					if (nums > 0) {
						toReturn.put("Result", nums + " player accounts were affected.");
					} else {
						toReturn.put("Error", nums + " player accounts were affected.");
					}

					return toReturn;
				}

				toReturn.put("Error", emailAddress + " is an invalid email.");
			} catch (IOException var16) {
				toReturn.put("Error", "Failed to load player data. Password not changed.");
				logger.log(Level.WARNING, var16.getMessage(), var16);
			}
		}

		return toReturn;
	}

	public Map changeEmail(String intraServerPassword, String playerName, String oldEmailAddress,
			String newEmailAddress) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		toReturn.put("Result", "Unknown email.");
		logger.log(Level.INFO, this.getRemoteClientDetails() + " Change Email for player name: " + playerName);
		if (Constants.maintaining) {
			toReturn.put("Error", "The server is currently unavailable.");
			toReturn.put("Result", "The server is in maintenance mode. Please try later.");
			return toReturn;
		} else {
			int nums;
			if (oldEmailAddress != null) {
				if (!isEmailValid(oldEmailAddress)) {
					toReturn.put("Error", "The old email address, " + oldEmailAddress + " is an invalid email.");
				} else if (!isEmailValid(newEmailAddress)) {
					toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
				} else {
					PlayerInfo[] var11 = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
					int var12 = 0;

					for (nums = 0; nums < var11.length; ++nums) {
						if (var11[nums].getPower() == 0) {
							var11[nums].setEmailAddress(newEmailAddress);
							++var12;
							toReturn.put("Account" + var12, var11[nums].getName() + " account was affected.");
						} else {
							toReturn.put("Account" + var12, var11[nums].getName() + " account was _not_ affected.");
						}
					}

					if (var12 > 0) {
						toReturn.put("Result", var12 + " player accounts were affected.");
					} else {
						toReturn.put("Error", var12 + " player accounts were affected.");
					}
				}

				return toReturn;
			} else {
				if (playerName != null) {
					PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

					try {
						p.load();
						if (isEmailValid(newEmailAddress)) {
							oldEmailAddress = p.emailAddress;
							PlayerInfo[] iox = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
							nums = 0;

							for (int x = 0; x < iox.length; ++x) {
								if (iox[x].getPower() == 0) {
									iox[x].setEmailAddress(newEmailAddress);
									++nums;
									toReturn.put("Account" + nums, iox[x].getName() + " account was affected.");
								} else {
									toReturn.put("Account" + nums, iox[x].getName() + " account was _not_ affected.");
								}
							}

							if (nums > 0) {
								toReturn.put("Result", nums + " player accounts were affected.");
							} else {
								toReturn.put("Error", nums + " player accounts were affected.");
							}

							return toReturn;
						}

						toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
					} catch (IOException var10) {
						toReturn.put("Error", "Failed to load player data. Email not changed.");
						logger.log(Level.WARNING, var10.getMessage(), var10);
					}
				}

				return toReturn;
			}
		}
	}

	public String getChallengePhrase(String intraServerPassword, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (playerName.contains("@")) {
			PlayerInfo[] p1 = PlayerInfoFactory.getPlayerInfosForEmail(playerName);
			return p1.length > 0 ? p1[0].pwQuestion : "Incorrect email.";
		} else {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer(this.getRemoteClientDetails() + " getChallengePhrase for player name: " + playerName);
			}

			PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

			try {
				p.load();
				return p.pwQuestion;
			} catch (IOException var5) {
				logger.log(Level.WARNING, var5.getMessage(), var5);
				return "Error";
			}
		}
	}

	public String[] getPlayerNamesForEmail(String intraServerPassword, String emailAddress) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getPlayerNamesForEmail: " + emailAddress);
		}

		String[] nameArray = PlayerInfoFactory.getAccountsForEmail(emailAddress);
		return nameArray;
	}

	public String getEmailAddress(String intraServerPassword, String playerName) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getEmailAddress for player name: " + playerName);
		}

		PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

		try {
			p.load();
			return p.emailAddress;
		} catch (IOException var5) {
			logger.log(Level.WARNING, var5.getMessage(), var5);
			return "Error";
		}
	}

	public static String generateRandomPassword() {
		Random rand = new Random();
		int length = rand.nextInt(3) + 6;
		char[] password = new char[length];

		for (int x = 0; x < length; ++x) {
			int randDecimalAsciiVal = rand.nextInt("abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".length());
			password[x] = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".charAt(randDecimalAsciiVal);
		}

		return String.valueOf(password);
	}

	public static final boolean isEmailValid(String emailAddress) {
		if (emailAddress == null) {
			return false;
		} else {
			Matcher m = VALID_EMAIL_PATTERN.matcher(emailAddress);
			return m.matches();
		}
	}

	public Map requestPasswordReset(String intraServerPassword, String email, String challengePhraseAnswer)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		if (Constants.maintaining) {
			toReturn.put("Error0", "The server is currently in maintenance mode.");
			return toReturn;
		} else {
			boolean ok = false;
			String password = generateRandomPassword();
			String playernames = "";
			logger.log(Level.INFO, this.getRemoteClientDetails() + " Password reset for email/name: " + email);
			if (challengePhraseAnswer != null && challengePhraseAnswer.length() >= 1) {
				if (!email.contains("@")) {
					PlayerInfo ex = PlayerInfoFactory.createPlayerInfo(email);
					if (!ex.loaded) {
						try {
							ex.load();
							logger.log(Level.INFO, email + " " + challengePhraseAnswer + " compares to " + ex.pwAnswer);
							if (System.currentTimeMillis() - ex.lastRequestedPassword <= 60000L) {
								toReturn.put("Error", "Please try again in a minute.");
								return toReturn;
							}

							logger.log(Level.INFO, email + " time ok. comparing.");
							if (ex.pwAnswer.equalsIgnoreCase(challengePhraseAnswer)) {
								logger.log(Level.INFO, email + " challenge answer correct.");
								ok = true;
								playernames = ex.getName();
								ex.updatePassword(password);
								if (ex.currentServer != Servers.localServer.id) {
									new PasswordTransfer(ex.getName(), ex.wurmId, ex.getPassword(),
											System.currentTimeMillis(), false);
								}
							}

							ex.lastRequestedPassword = System.currentTimeMillis();
						} catch (IOException var12) {
							logger.log(Level.WARNING, email + ":" + var12.getMessage(), var12);
							toReturn.put("Error", "An error occured. Please try later.");
							return toReturn;
						}
					}
				} else {
					PlayerInfo[] var14 = PlayerInfoFactory.getPlayerInfosWithEmail(email);

					for (int x = 0; x < var14.length; ++x) {
						try {
							var14[x].load();
							if (var14[x].pwAnswer.toLowerCase().equals(challengePhraseAnswer.toLowerCase())
									|| var14[x].pwAnswer.length() == 0 && var14[x].pwQuestion.length() == 0) {
								if (System.currentTimeMillis() - var14[x].lastRequestedPassword > 60000L) {
									ok = true;
									if (playernames.length() > 0) {
										playernames = playernames + ", " + var14[x].getName();
									} else {
										playernames = var14[x].getName();
									}

									var14[x].updatePassword(password);
									if (var14[x].currentServer != Servers.localServer.id) {
										new PasswordTransfer(var14[x].getName(), var14[x].wurmId,
												var14[x].getPassword(), System.currentTimeMillis(), false);
									}
								} else if (!ok) {
									toReturn.put("Error", "Please try again in a minute.");
									return toReturn;
								}
							}

							var14[x].lastRequestedPassword = System.currentTimeMillis();
						} catch (IOException var13) {
							logger.log(Level.WARNING, email + ":" + var13.getMessage(), var13);
							toReturn.put("Error", "An error occured. Please try later.");
							return toReturn;
						}
					}
				}

				if (ok) {
					toReturn.put("Result", "Password was changed.");
				} else {
					toReturn.put("Error", "Password was not changed.");
				}

				if (playernames.length() > 0) {
					try {
						String var15 = Mailer.getPasswordMail();
						var15 = var15.replace("@pname", playernames);
						var15 = var15.replace("@password", password);
						Mailer.sendMail(mailAccount, email, "Wurm Online password request", var15);
						toReturn.put("MailResult",
								"A mail was sent to the mail adress: " + email + " for " + playernames + ".");
					} catch (Exception var11) {
						logger.log(Level.WARNING, email + ":" + var11.getMessage(), var11);
						toReturn.put("MailError", "An error occured - " + var11.getMessage() + ". Please try later.");
					}

					return toReturn;
				} else {
					toReturn.put("Error", "Wrong answer.");
					return toReturn;
				}
			} else {
				toReturn.put("Error0", "The answer is too short.");
				return toReturn;
			}
		}
	}

	public Map getAllServers(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		return this.getAllServerInternalAddresses(intraServerPassword);
	}

	public Map getAllServerInternalAddresses(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		HashMap toReturn = new HashMap();
		ServerEntry[] entries = Servers.getAllServers();

		for (int x = 0; x < entries.length; ++x) {
			toReturn.put(Integer.valueOf(entries[x].id), entries[x].INTRASERVERADDRESS);
		}

		return toReturn;
	}

	public boolean sendMail(String intraServerPassword, String sender, String receiver, String subject, String text)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (!isEmailValid(sender)) {
			return false;
		} else if (!isEmailValid(receiver)) {
			return false;
		} else {
			try {
				Mailer.sendMail(sender, receiver, subject, text);
				return true;
			} catch (Exception var7) {
				logger.log(Level.WARNING, var7.getMessage(), var7);
				return false;
			}
		}
	}

	public void shutDown(String intraServerPassword, String playerName, String password, String reason, int seconds)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(this.getRemoteClientDetails() + " shutDown by player name: " + playerName);
		}

		PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(playerName));

		try {
			pinf.load();
			if (pinf.getPower() >= 4) {
				try {
					String iox = LoginHandler.hashPassword(password,
							LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.getName())));
					if (iox.equals(pinf.getPassword())) {
						logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName
								+ " initiated shutdown in " + seconds + " seconds: " + reason);
						if (seconds <= 0) {
							Server.getInstance().shutDown();
						} else {
							Server.getInstance().startShutdown(seconds, reason);
						}
					} else {
						logger.log(Level.WARNING, this.getRemoteClientDetails() + " player: " + playerName
								+ " denied shutdown due to wrong password.");
					}
				} catch (Exception var8) {
					logger.log(Level.INFO, "Failed to encrypt password for player " + playerName, var8);
				}
			} else {
				logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + " DENIED shutdown in "
						+ seconds + " seconds: " + reason);
			}
		} catch (IOException var9) {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + ": " + var9.getMessage(),
					var9);
		}

	}

	public Map getReferrers(String intraServerPassword, long wurmid) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getReferrers for WurmID: " + wurmid);
		}

		return PlayerInfoFactory.getReferrers(wurmid);
	}

	public String addReferrer(String intraServerPassword, String receiver, long referrer) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.info(this.getRemoteClientDetails() + " addReferrer for Receiver player name: " + receiver
				+ ", referrerID: " + referrer);
		Object var5 = Server.SYNC_LOCK;
		synchronized (Server.SYNC_LOCK) {
			String var10000;
			try {
				PlayerInfo e = PlayerInfoFactory.createPlayerInfo(receiver);

				try {
					e.load();
				} catch (IOException var8) {
					var10000 = receiver + " - no such player exists. Please check the spelling.";
					return var10000;
				}

				if (e.wurmId == referrer) {
					return "You may not refer yourself.";
				}

				if (e.getPaymentExpire() > 0L) {
					if (!PlayerInfoFactory.addReferrer(e.wurmId, referrer)) {
						return "You have already awarded referral to that player.";
					}

					var10000 = String.valueOf(e.wurmId);
					return var10000;
				}

				var10000 = e.getName() + " has never had a premium account and may not receive referrals.";
			} catch (Exception var9) {
				logger.log(Level.WARNING, var9.getMessage() + " " + receiver + " from " + referrer, var9);
				return "An error occurred. Please write a bug report about this.";
			}

			return var10000;
		}
	}

	public String acceptReferrer(String intraServerPassword, long wurmid, String awarderName, boolean money)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(this.getRemoteClientDetails() + " acceptReferrer for player wurmid: " + wurmid
					+ ", awarderName: " + awarderName + ", money: " + money);
		}

		PlayerInfo pinf = null;

		try {
			long ex = Long.parseLong(awarderName);
			pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(ex);
		} catch (NumberFormatException var12) {
			pinf = PlayerInfoFactory.createPlayerInfo(awarderName);

			try {
				pinf.load();
			} catch (IOException var11) {
				logger.log(Level.WARNING, var11.getMessage(), var11);
				return "Failed to locate the player " + awarderName + " in the database.";
			}
		}

		if (pinf != null) {
			try {
				Object ex1 = Server.SYNC_LOCK;
				synchronized (Server.SYNC_LOCK) {
					if (!PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
						return "Failed to match " + awarderName + " to any existing referral.";
					}

					try {
						if (money) {
							PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Referred by " + pinf.getName());
						} else {
							PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Referred by " + pinf.getName());
						}
					} catch (Exception var10) {
						logger.log(Level.WARNING, var10.getMessage(), var10);
						PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
						return "An error occured. Please try later or post a bug report.";
					}
				}
			} catch (Exception var14) {
				logger.log(Level.WARNING, var14.getMessage(), var14);
				return "An error occured. Please try later or post a bug report.";
			}

			return "Okay, accepted the referral from " + awarderName
					+ ". The reward will arrive soon if it has not already.";
		} else {
			return "Failed to locate " + awarderName + " in the database.";
		}
	}

	public Map getSkillStats(String intraServerPassword, int skillid) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getSkillStats for skillid: " + skillid);
		}

		HashMap toReturn = new HashMap();

		try {
			SkillStat ex = SkillStat.getSkillStatForSkill(skillid);
			Iterator it = ex.stats.entrySet().iterator();

			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				Long lid = (Long) entry.getKey();
				long pid = lid.longValue();
				PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
				if (p != null && ((Double) entry.getValue()).doubleValue() > 1.0D) {
					toReturn.put(p.getName(), (Double) entry.getValue());
				}
			}
		} catch (Exception var11) {
			logger.log(Level.WARNING, var11.getMessage(), var11);
			toReturn.put("ERROR: " + var11.getMessage(), Double.valueOf(0.0D));
		}

		return toReturn;
	}

	public Map getSkills(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		return SkillSystem.skillNames;
	}

	public Map getStructureSummary(String intraServerPassword, long aStructureID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getStructureSummary for StructureID: " + aStructureID);
		}

		HashMap lToReturn = new HashMap(10);

		try {
			Structure e = Structures.getStructure(aStructureID);
			if (e != null) {
				lToReturn.put("CenterX", Integer.valueOf(e.getCenterX()));
				lToReturn.put("CenterY", Integer.valueOf(e.getCenterY()));
				lToReturn.put("CreationDate", Long.valueOf(e.getCreationDate()));
				lToReturn.put("Door Count", Integer.valueOf(e.getDoors()));
				lToReturn.put("FinalFinished", Boolean.valueOf(e.isFinalFinished()));
				lToReturn.put("Finalized", Boolean.valueOf(e.isFinalized()));
				lToReturn.put("Finished", Boolean.valueOf(e.isFinished()));
				lToReturn.put("Guest Count", Integer.valueOf(e.getPermissionsPlayerList().size()));
				lToReturn.put("Limit", Integer.valueOf(e.getLimit()));
				lToReturn.put("Lockable", Boolean.valueOf(e.isLockable()));
				lToReturn.put("Locked", Boolean.valueOf(e.isLocked()));
				lToReturn.put("MaxX", Integer.valueOf(e.getMaxX()));
				lToReturn.put("MaxY", Integer.valueOf(e.getMaxY()));
				lToReturn.put("MinX", Integer.valueOf(e.getMinX()));
				lToReturn.put("MinY", Integer.valueOf(e.getMinY()));
				lToReturn.put("Name", e.getName());
				lToReturn.put("OwnerID", Long.valueOf(e.getOwnerId()));
				lToReturn.put("Roof", Byte.valueOf(e.getRoof()));
				lToReturn.put("Size", Integer.valueOf(e.getSize()));
				lToReturn.put("HasWalls", Boolean.valueOf(e.hasWalls()));
				Wall[] lWalls = e.getWalls();
				if (lWalls != null) {
					lToReturn.put("Wall Count", Integer.valueOf(lWalls.length));
				} else {
					lToReturn.put("Wall Count", Integer.valueOf(0));
				}

				lToReturn.put("WritID", Long.valueOf(e.getWritId()));
				lToReturn.put("WurmID", Long.valueOf(e.getWurmId()));
			} else {
				lToReturn.put("Error", "No such Structure");
			}
		} catch (NoSuchStructureException var7) {
			logger.log(Level.WARNING, "Structure with id " + aStructureID + " not found.", var7);
			lToReturn.put("Error", "No such Structure");
			lToReturn.put("Exception", var7.getMessage());
		} catch (RuntimeException var8) {
			logger.log(Level.WARNING, "Error: " + var8.getMessage(), var8);
			lToReturn.put("Exception", var8);
		}

		return lToReturn;
	}

	public long getStructureIdFromWrit(String intraServerPassword, long aWritID) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getStructureIdFromWrit for WritID: " + aWritID);
		}

		try {
			Structure struct = Structures.getStructureForWrit(aWritID);
			if (struct != null) {
				return struct.getWurmId();
			}
		} catch (NoSuchStructureException var5) {
			;
		}

		return -1L;
	}

	public Map getTileSummary(String intraServerPassword, int tilex, int tiley, boolean surfaced)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getTileSummary for tile (x,y): " + tilex + ", " + tiley);
		}

		HashMap lToReturn = new HashMap(10);

		try {
			Zone e = Zones.getZone(tilex, tiley, surfaced);
			VolaTile tile = e.getTileOrNull(tilex, tiley);
			if (tile != null) {
				Structure lStructure = tile.getStructure();
				if (lStructure != null) {
					lToReturn.put("StructureID", Long.valueOf(lStructure.getWurmId()));
					lToReturn.put("StructureName", lStructure.getName());
				}

				lToReturn.put("Kingdom", Byte.valueOf(tile.getKingdom()));
				Village lVillage = tile.getVillage();
				if (lVillage != null) {
					lToReturn.put("VillageID", Integer.valueOf(lVillage.getId()));
					lToReturn.put("VillageName", lVillage.getName());
				}

				lToReturn.put("Coord x", Integer.valueOf(tile.getTileX()));
				lToReturn.put("Coord y", Integer.valueOf(tile.getTileY()));
			} else {
				lToReturn.put("Error", "No such tile");
			}
		} catch (NoSuchZoneException var10) {
			lToReturn.put("Error", "No such zone");
			lToReturn.put("Exception", var10.getMessage());
		} catch (RuntimeException var11) {
			logger.log(Level.WARNING, "Error: " + var11.getMessage(), var11);
			lToReturn.put("Exception", var11);
		}

		return lToReturn;
	}

	public String getReimbursementInfo(String intraServerPassword, String email) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getReimbursementInfo for email: " + email);
		}

		return Reimbursement.getReimbursementInfo(email);
	}

	public boolean withDraw(String intraServerPassword, String retriever, String name, String _email, int _months,
			int _silvers, boolean titlebok, int _daysLeft) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.info(this.getRemoteClientDetails() + " withDraw for retriever: " + retriever + ", name: " + name
				+ ", email: " + _email + ", months: " + _months + ", silvers: " + _silvers);
		return Reimbursement.withDraw(retriever, name, _email, _months, _silvers, titlebok, _daysLeft);
	}

	public boolean transferPlayer(String intraServerPassword, String playerName, int posx, int posy, boolean surfaced,
			int power, byte[] data) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (Constants.maintaining && power <= 0) {
			return false;
		} else {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " Transferplayer name: " + playerName
					+ ", position (x,y): " + posx + ", " + posy + ", surfaced: " + surfaced);
			return IntraServerConnection.savePlayerToDisk(data, posx, posy, surfaced, false) > 0L
					? (!Servers.isThisLoginServer()
							? (new LoginServerWebConnection()).setCurrentServer(playerName, Servers.localServer.id)
							: true)
					: false;
		}
	}

	public boolean changePassword(String intraServerPassword, long wurmId, String newPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword name: " + wurmId);
		return IntraServerConnection.setNewPassword(wurmId, newPassword);
	}

	public boolean setCurrentServer(String intraServerPassword, String name, int currentServer) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " setCurrentServer to " + currentServer + " for player name: "
					+ name);
		}

		PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
		if (pinf == null) {
			return false;
		} else {
			pinf.setCurrentServer(currentServer);
			return true;
		}
	}

	public boolean addDraggedItem(String intraServerPassword, long itemId, byte[] itemdata, long draggerId, int posx,
			int posy) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
		logger.log(Level.INFO,
				this.getRemoteClientDetails() + " Adddraggeditem itemID: " + itemId + ", draggerId: " + draggerId);

		try {
			HashSet nsz = new HashSet();
			int z = iis.readInt();
			int x = 0;

			while (true) {
				if (x >= z) {
					Items.convertItemMetaData((ItemMetaData[]) nsz.toArray(new ItemMetaData[nsz.size()]));
					break;
				}

				IntraServerConnection.createItem(iis, 0.0F, 0.0F, 0.0F, nsz, false);
				++x;
			}
		} catch (IOException var15) {
			logger.log(Level.WARNING, var15.getMessage(), var15);
			return false;
		}

		try {
			Item var16 = Items.getItem(itemId);
			Zone var17 = Zones.getZone(posx, posy, true);
			var17.addItem(var16);
			return true;
		} catch (NoSuchItemException var13) {
			logger.log(Level.WARNING, var13.getMessage(), var13);
			return false;
		} catch (NoSuchZoneException var14) {
			logger.log(Level.WARNING, var14.getMessage(), var14);
			return false;
		}
	}

	public String rename(String intraServerPassword, String oldName, String newName, String newPass, int power)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " rename oldName: " + oldName + ", newName: " + newName
					+ ", power: " + power);
		}

		String toReturn = "";
		newName = LoginHandler.raiseFirstLetter(newName);
		if (Servers.localServer.LOGINSERVER && Players.getInstance().doesPlayerNameExist(newName)) {
			return "The name " + newName + " already exists. This is an Error.";
		} else {
			if (Servers.localServer.LOGINSERVER) {
				toReturn = toReturn + Servers.rename(oldName, newName, newPass, power);
			}

			if (!toReturn.contains("Error.")) {
				try {
					toReturn = PlayerInfoFactory.rename(oldName, newName, newPass, power);
				} catch (IOException var8) {
					toReturn = toReturn + Servers.localServer.name + " " + var8.getMessage() + ". This is an Error.\n";
					logger.log(Level.WARNING, var8.getMessage(), var8);
				}
			}

			return toReturn;
		}
	}

	public String changePassword(String intraServerPassword, String changerName, String name, String newPass, int power)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " changePassword, changerName: " + changerName
					+ ", for player name: " + name + ", power: " + power);
		}

		String toReturn = "";
		changerName = LoginHandler.raiseFirstLetter(changerName);
		name = LoginHandler.raiseFirstLetter(name);

		try {
			toReturn = PlayerInfoFactory.changePassword(changerName, name, newPass, power);
		} catch (IOException var9) {
			toReturn = toReturn + Servers.localServer.name + " " + var9.getMessage() + "\n";
			logger.log(Level.WARNING, var9.getMessage(), var9);
		}

		logger.log(Level.INFO, this.getRemoteClientDetails() + " changePassword, changerName: " + changerName
				+ ", for player name: " + name);
		if (Servers.localServer.LOGINSERVER) {
			if (changerName.equals(name)) {
				PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
				if (pinf != null && Servers.localServer.id != pinf.currentServer) {
					LoginServerWebConnection lsw = new LoginServerWebConnection(pinf.currentServer);
					toReturn = toReturn + lsw.changePassword(changerName, name, newPass, power);
				}
			} else {
				toReturn = toReturn + Servers.sendChangePass(changerName, name, newPass, power);
			}
		}

		return toReturn;
	}

	public String changeEmail(String intraServerPassword, String changerName, String name, String newEmail,
			String password, int power, String pwQuestion, String pwAnswer) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName
					+ ", for player name: " + name + ", power: " + power);
		}

		changerName = LoginHandler.raiseFirstLetter(changerName);
		name = LoginHandler.raiseFirstLetter(name);
		String toReturn = "";
		logger.log(Level.INFO, this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName
				+ ", for player name: " + name);

		try {
			toReturn = PlayerInfoFactory.changeEmail(changerName, name, newEmail, password, power, pwQuestion,
					pwAnswer);
			if (toReturn.equals("NO") || toReturn.equals("NO Retrieval info updated.")) {
				return "You may only have 5 accounts with the same email. Also you need to provide the correct password for a character with that email address in order to change to it.";
			}
		} catch (IOException var11) {
			toReturn = toReturn + Servers.localServer.name + " " + var11.getMessage() + "\n";
			logger.log(Level.WARNING, var11.getMessage(), var11);
		}

		if (Servers.localServer.LOGINSERVER) {
			toReturn = toReturn
					+ Servers.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
		}

		return toReturn;
	}

	public String addReimb(String intraServerPassword, String changerName, String name, int numMonths, int _silver,
			int _daysLeft, boolean setbok) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(this.getRemoteClientDetails() + " addReimb, changerName: " + changerName + ", for player name: "
					+ name + ", numMonths: " + numMonths + ", silver: " + _silver + ", daysLeft: " + _daysLeft
					+ ", setbok: " + setbok);
		}

		changerName = LoginHandler.raiseFirstLetter(changerName);
		name = LoginHandler.raiseFirstLetter(name);
		return Servers.localServer.LOGINSERVER
				? Reimbursement.addReimb(changerName, name, numMonths, _silver, _daysLeft, setbok)
				: Servers.localServer.name + " - failed to add reimbursement. This is not the login server.";
	}

	public long[] getCurrentServerAndWurmid(String intraServerPassword, String name, long wurmid)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " getCurrentServerAndWurmid for player name: " + name
					+ ", wurmid: " + wurmid);
		}

		PlayerInfo pinf = null;
		if (name != null && name.length() > 2) {
			name = LoginHandler.raiseFirstLetter(name);
			pinf = PlayerInfoFactory.createPlayerInfo(name);
		} else if (wurmid > 0L) {
			pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
		}

		if (pinf != null) {
			try {
				pinf.load();
				long[] toReturn = new long[] { (long) pinf.currentServer, pinf.wurmId };
				return toReturn;
			} catch (IOException var7) {
				;
			}
		}

		return noInfoLong;
	}

	public Map getPlayerStates(String intraServerPassword, long[] wurmids) throws RemoteException, WurmServerException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			if (wurmids.length == 0) {
				logger.finer(this.getRemoteClientDetails() + " getPlayersSubInfo for ALL players.");
			} else {
				StringBuilder buf = new StringBuilder();

				for (int x = 0; x < wurmids.length; ++x) {
					if (x > 0) {
						buf.append(",");
					}

					buf.append(wurmids[x]);
				}

				logger.finer(
						this.getRemoteClientDetails() + " getPlayersSubInfo for player wurmids: " + buf.toString());
			}
		}

		return PlayerInfoFactory.getPlayerStates(wurmids);
	}

	public void manageFeature(String intraServerPassword, int serverId, final int featureId, final boolean aOverridden,
			final boolean aEnabled, final boolean global) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " manageFeature " + featureId);
		}

		Thread t = new Thread("manageFeature-Thread-" + featureId) {
			public void run() {
				Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, aOverridden, aEnabled, global);
			}
		};
		t.setPriority(4);
		t.start();
	}

	public void startShutdown(String intraServerPassword, String instigator, int seconds, String reason)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (Servers.isThisLoginServer()) {
			Servers.startShutdown(instigator, seconds, reason);
		}

		logger.log(Level.INFO, instigator + " shutting down server in " + seconds + " seconds, reason: " + reason);
		Server.getInstance().startShutdown(seconds, reason);
	}

	public String sendMail(String intraServerPassword, byte[] maildata, byte[] itemdata, long sender, long wurmid,
			int targetServer) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.log(Level.INFO, this.getRemoteClientDetails() + " sendMail " + sender + " to server " + targetServer
				+ ", receiver ID: " + wurmid);
		if (targetServer == Servers.localServer.id) {
			DataInputStream var16 = new DataInputStream(new ByteArrayInputStream(maildata));

			try {
				int var17 = var16.readInt();

				for (int iox = 0; iox < var17; ++iox) {
					WurmMail nums = new WurmMail(var16.readByte(), var16.readLong(), var16.readLong(), var16.readLong(),
							var16.readLong(), var16.readLong(), var16.readLong(), var16.readInt(), var16.readBoolean(),
							false);
					WurmMail.addWurmMail(nums);
					nums.createInDatabase();
				}
			} catch (IOException var15) {
				logger.log(Level.WARNING, var15.getMessage(), var15);
				return "A database error occurred. Please report this to a GM.";
			}

			DataInputStream var18 = new DataInputStream(new ByteArrayInputStream(itemdata));

			try {
				HashSet var19 = new HashSet();
				int var20 = var18.readInt();

				for (int x = 0; x < var20; ++x) {
					IntraServerConnection.createItem(var18, 0.0F, 0.0F, 0.0F, var19, false);
				}

				Items.convertItemMetaData((ItemMetaData[]) var19.toArray(new ItemMetaData[var19.size()]));
				return "";
			} catch (IOException var14) {
				logger.log(Level.WARNING, var14.getMessage(), var14);
				return "A database error occurred when inserting an item. Please report this to a GM.";
			}
		} else {
			ServerEntry entry = Servers.getServerWithId(targetServer);
			if (entry != null) {
				if (entry.isAvailable(5, true)) {
					LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
					return lsw.sendMail(maildata, itemdata, sender, wurmid, targetServer);
				} else {
					return "The target server is not available right now.";
				}
			} else {
				return "Failed to locate target server.";
			}
		}
	}

	public String pardonban(String intraServerPassword, String name) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " pardonban for player name: " + name);
		}

		if (Servers.localServer.LOGINSERVER) {
			PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
			if (info != null) {
				try {
					info.load();
				} catch (IOException var6) {
					logger.log(
							Level.WARNING, this.getRemoteClientDetails()
									+ " Failed to load the player information. Not pardoned - " + var6.getMessage(),
							var6);
					return "Failed to load the player information. Not pardoned.";
				}

				try {
					info.setBanned(false, "", 0L);
				} catch (IOException var5) {
					logger.log(
							Level.WARNING, this.getRemoteClientDetails()
									+ " Failed to save the player information. Not pardoned - " + var5.getMessage(),
							var5);
					return "Failed to save the player information. Not pardoned.";
				}

				logger.info(this.getRemoteClientDetails() + " Login server pardoned " + name);
				return "Login server pardoned " + name + ".";
			} else {
				logger.warning("Failed to locate the player " + name + ".");
				return "Failed to locate the player " + name + ".";
			}
		} else {
			logger.warning(Servers.localServer.name + " not login server. Pardon failed.");
			return Servers.localServer.name + " not login server. Pardon failed.";
		}
	}

	public String ban(String intraServerPassword, String name, String reason, int days) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer(this.getRemoteClientDetails() + " ban for player name: " + name + ", reason: " + reason
					+ ", for " + days + " days");
		}

		if (Servers.localServer.LOGINSERVER) {
			PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
			if (info != null) {
				long expiry = System.currentTimeMillis() + (long) days * 86400000L;

				try {
					info.load();
				} catch (IOException var10) {
					logger.log(Level.WARNING,
							"Failed to load the player information. Not banned - " + var10.getMessage(), var10);
					return "Failed to load the player information. Not banned.";
				}

				try {
					info.setBanned(true, reason, expiry);
				} catch (IOException var9) {
					logger.log(Level.WARNING,
							"Failed to save the player information. Not banned - " + var9.getMessage(), var9);
					return "Failed to save the player information. Not banned.";
				}

				logger.info(this.getRemoteClientDetails() + " Login server banned " + name + ": " + reason + " for "
						+ days + " days.");
				return "Login server banned " + name + ": " + reason + " for " + days + " days.";
			} else {
				logger.warning("Failed to locate the player " + name + ".");
				return "Failed to locate the player " + name + ".";
			}
		} else {
			logger.warning(Servers.localServer.name + " not login server. Ban failed.");
			return Servers.localServer.name + " not login server. Ban failed.";
		}
	}

	public String addBannedIp(String intraServerPassword, String ip, String reason, int days) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		long expiry = System.currentTimeMillis() + (long) days * 86400000L;
		Players.getInstance().addBannedIp(ip, reason, expiry);
		logger.info(this.getRemoteClientDetails() + " RMI client requested " + ip + " banned for " + days + " days - "
				+ reason);
		return ip + " banned for " + days + " days - " + reason;
	}

	public BannedIp[] getPlayersBanned(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		return Players.getInstance().getPlayersBanned();
	}

	public BannedIp[] getIpsBanned(String intraServerPassword) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		return Players.getInstance().getIpsBanned();
	}

	public String removeBannedIp(String intraServerPassword, String ip) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (Players.getInstance().removeBannedIp(ip)) {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client requested " + ip + " was pardoned.");
			return "Okay, " + ip + " was pardoned.";
		} else {
			logger.info(this.getRemoteClientDetails() + " RMI client requested pardon but the ip " + ip
					+ " was not previously banned.");
			return "The ip " + ip + " was not previously banned.";
		}
	}

	public String setPlayerMoney(String intraServerPassword, long wurmid, long currentMoney, long moneyAdded,
			String detail) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (moneyDetails.contains(detail)) {
			logger.warning(this.getRemoteClientDetails()
					+ " RMI client The money transaction has already been performed, wurmid: " + wurmid
					+ ", currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
			return "The money transaction has already been performed";
		} else {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set player money for " + wurmid);
			PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
			if (info != null) {
				try {
					info.load();
				} catch (IOException var13) {
					logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ", detail: " + detail + ": "
							+ var13.getMessage(), var13);
					return "Failed to load the player from database. Transaction failed.";
				}

				if (info.wurmId <= 0L) {
					logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney
							+ ", detail: " + detail + "!");
					return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
				} else if (info.currentServer != Servers.localServer.id) {
					logger.warning("Received a CMD_SET_PLAYER_MONEY for player " + info.getName() + " (id: " + wurmid
							+ ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: "
							+ Servers.localServer.id + "), detail: " + detail);
					return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
				} else {
					try {
						info.setMoney(currentMoney);
						new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, (byte) 6, "");
						Change iox = new Change(currentMoney);
						moneyDetails.add(detail);

						try {
							logger.info(this.getRemoteClientDetails() + " RMI client Added " + moneyAdded
									+ " to player ID: " + wurmid + ", currentMoney: " + currentMoney + ", detail: "
									+ detail);
							Player exp = Players.getInstance().getPlayer(wurmid);
							Message mess = new Message((Creature) null, (byte) 3, ":Event",
									"Your available money in the bank is now " + iox.getChangeString() + ".");
							mess.setReceiver(exp.getWurmId());
							Server.getInstance().addMessage(mess);
						} catch (NoSuchPlayerException var14) {
							if (logger.isLoggable(Level.FINER)) {
								logger.finer("player ID: " + wurmid + " is not online, currentMoney: " + currentMoney
										+ ", moneyAdded: " + moneyAdded + ", detail: " + detail);
							}
						}

						return "Okay. The player now has " + iox.getChangeString() + " in the bank.";
					} catch (IOException var15) {
						logger.log(Level.WARNING,
								wurmid + ", failed to set money to " + currentMoney + ", detail: " + detail + ".",
								var15);
						return "Money transaction failed. Error reported was " + var15.getMessage() + ".";
					}
				}
			} else {
				logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney
						+ ", detail: " + detail + "!");
				return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
			}
		}
	}

	public String setPlayerPremiumTime(String intraServerPassword, long wurmid, long currentExpire, int days,
			int months, String detail) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (timeDetails.contains(detail)) {
			logger.warning(this.getRemoteClientDetails()
					+ " RMI client The time transaction has already been performed, wurmid: " + wurmid
					+ ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: "
					+ detail);
			return "The time transaction has already been performed";
		} else {
			logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set premium time for " + wurmid);
			PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
			if (info != null) {
				try {
					info.load();
				} catch (IOException var15) {
					logger.log(Level.WARNING,
							"Failed to load the player from database. Transaction failed, wurmid: " + wurmid
									+ ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months
									+ ", detail: " + detail,
							var15);
					return "Failed to load the player from database. Transaction failed.";
				}

				if (info.currentServer != Servers.localServer.id) {
					logger.warning("Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player " + info.getName() + " (id: "
							+ wurmid + ") but their currentserver (id: " + info.getCurrentServer()
							+ ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
					return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
				} else {
					try {
						info.setPaymentExpire(currentExpire);
						new TimeTransfer(info.getName(), wurmid, months, false, days, detail);
						timeDetails.add(detail);

						try {
							Player iox = Players.getInstance().getPlayer(wurmid);
							String expireString = "You now have premier playing time until "
									+ WurmCalendar.formatGmt(currentExpire) + ".";
							Message mess = new Message((Creature) null, (byte) 3, ":Event", expireString);
							mess.setReceiver(iox.getWurmId());
							Server.getInstance().addMessage(mess);
						} catch (NoSuchPlayerException var13) {
							;
						}

						logger.info(this.getRemoteClientDetails() + " RMI client " + info.getName()
								+ " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire)
								+ ", wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days
								+ ", months: " + months + ", detail: " + detail + '.');
						return "Okay. " + info.getName() + " now has premier playing time until "
								+ WurmCalendar.formatGmt(currentExpire) + ".";
					} catch (IOException var14) {
						logger.log(Level.WARNING,
								"Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire
										+ ", days: " + days + ", months: " + months + ", detail: " + detail + ", "
										+ var14.getMessage(),
								var14);
						return "Time transaction failed. Error reported was " + var14.getMessage() + ".";
					}
				}
			} else {
				logger.log(Level.WARNING, wurmid + ", failed to locate player info and set expire time to "
						+ currentExpire + "!, detail: " + detail);
				return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
			}
		}
	}

	public void setWeather(String intraServerPassword, float windRotation, float windpower, float windDir)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Server.getWeather().setWindOnly(windRotation, windpower, windDir);
		logger.log(Level.INFO, this.getRemoteClientDetails()
				+ " RMI client. Received weather data from login server. Propagating windrot=" + windRotation);
		Players.getInstance().setShouldSendWeather(true);
	}

	public String sendVehicle(String intraServerPassword, byte[] passengerdata, byte[] itemdata, long pilotId,
			long vehicleId, int targetServer, int tilex, int tiley, int layer, float rot) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.log(Level.INFO,
				this.getRemoteClientDetails() + " RMI client send vehicle for pilot " + pilotId + " vehicle "
						+ vehicleId + " itemdata bytes=" + itemdata.length + " passenger data bytes="
						+ passengerdata.length);
		if (targetServer == Servers.localServer.id) {
			long var27 = System.nanoTime();
			DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
			HashSet idset = new HashSet();

			int x;
			try {
				int dis = iis.readInt();
				VehicleSettings.setSettings(vehicleId, dis, true);
				VehicleSettings.setBit(vehicleId, 5, true, true);
				int var29 = iis.readInt();
				logger.log(Level.INFO, "Trying to create " + var29 + " items for vehicle: " + vehicleId);
				float var32 = (float) (tilex * 4 + 2);
				float nums = (float) (tiley * 4 + 2);
				IntraServerConnection.resetTransferVariables(String.valueOf(pilotId));
				x = 0;

				while (true) {
					if (x >= var29) {
						Items.convertItemMetaData((ItemMetaData[]) idset.toArray(new ItemMetaData[idset.size()]));
						break;
					}

					IntraServerConnection.createItem(iis, var32, nums, 0.0F, idset, false);
					++x;
				}
			} catch (IOException var25) {
				logger.log(Level.WARNING, var25.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", "
						+ IntraServerConnection.lastItemId, var25);
				Iterator mt = idset.iterator();

				while (mt.hasNext()) {
					ItemMetaData lElapsedTime = (ItemMetaData) mt.next();
					logger.log(Level.INFO, lElapsedTime.itname + ", " + lElapsedTime.itemId);
				}

				return "A database error occurred when inserting an item. Please report this to a GM.";
			} catch (Exception var26) {
				logger.log(Level.WARNING, var26.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", "
						+ IntraServerConnection.lastItemId, var26);
				return "A database error occurred when inserting an item. Please report this to a GM.";
			}

			DataInputStream var28 = new DataInputStream(new ByteArrayInputStream(passengerdata));

			try {
				Item var30 = Items.getItem(vehicleId);
				var30.setPosXYZ((float) (tilex * 4 + 2), (float) (tiley * 4 + 2), 0.0F);
				var30.setRotation(rot);
				logger.log(Level.INFO, "Trying to put " + var30.getName() + ", " + var30.getDescription() + " at "
						+ var30.getTileX() + "," + var30.getTileY());
				Zones.getZone(var30.getTileX(), var30.getTileY(), layer == 0).addItem(var30);
				Vehicles.createVehicle(var30);
				MountTransfer var33 = new MountTransfer(vehicleId, pilotId);
				int var34 = var28.readInt();

				for (x = 0; x < var34; ++x) {
					var33.addToSeat(var28.readLong(), var28.readInt());
				}
			} catch (NoSuchItemException var22) {
				logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var22.getMessage(), var22);
			} catch (NoSuchZoneException var23) {
				logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var23.getMessage(), var23);
			} catch (IOException var24) {
				logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var24.getMessage(), var24);
				return "A database error occurred. Please report this to a GM.";
			}

			float var31 = (float) (System.nanoTime() - var27) / 1000000.0F;
			logger.log(Level.INFO, "Transferring vehicle " + vehicleId + " took " + var31 + " ms.");
			return "";
		} else {
			ServerEntry entry = Servers.getServerWithId(targetServer);
			if (entry != null) {
				if (entry.isAvailable(5, true)) {
					LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
					return lsw.sendVehicle(passengerdata, itemdata, pilotId, vehicleId, targetServer, tilex, tiley,
							layer, rot);
				} else {
					return "The target server is not available right now.";
				}
			} else {
				return "Failed to locate target server.";
			}
		}
	}

	public void genericWebCommand(final String intraServerPassword, final short wctype, final long id,
			final byte[] data) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		final WebCommand wc = WebCommand.createWebCommand(wctype, id, data);
		if (wc != null) {
			if (Servers.localServer.LOGINSERVER && wctype != 16 && wctype != 17 && wctype != 19 && wctype != 18
					&& wctype != 15 && wctype != 20 && wctype != 23 && wctype != 21 && wctype != 25 && wctype != 26
					&& wctype != 27) {
				Servers.sendWebCommandToAllServers(wctype, wc, wc.isEpicOnly());
			}

			if (WurmId.getOrigin(id) != Servers.localServer.id) {
				Server.getInstance().addWebCommand(wc);
			}
		}

	}

	public void setKingdomInfo(String intraServerPassword, int serverId, byte kingdomId, byte templateKingdom,
			String _name, String _password, String _chatName, String _suffix, String mottoOne, String mottoTwo,
			boolean acceptsPortals) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Kingdom newInfo = new Kingdom(kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne,
				mottoTwo, acceptsPortals);
		if (serverId != Servers.localServer.id) {
			Kingdoms.addKingdom(newInfo);
		}

		WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, kingdomId);
		wck.encode();
		Servers.sendWebCommandToAllServers((short) 7, wck, wck.isEpicOnly());
	}

	public boolean kingdomExists(String intraServerPassword, int serverId, byte kingdomId, boolean exists)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		logger.log(Level.INFO, "serverId:" + serverId + " kingdom id " + kingdomId + " exists=" + exists);
		boolean result = Servers.kingdomExists(serverId, kingdomId, exists);
		if (Servers.getServerWithId(serverId) != null && Servers.getServerWithId(serverId).name != null) {
			logger.log(Level.INFO,
					Servers.getServerWithId(serverId).name + " kingdom id " + kingdomId + " exists=" + exists);
		} else if (Servers.getServerWithId(serverId) == null) {
			logger.log(Level.INFO, serverId + " server is null " + kingdomId + " exists=" + exists);
		} else {
			logger.log(Level.INFO, "Name for " + Servers.getServerWithId(serverId) + " server is null " + kingdomId
					+ " exists=" + exists);
		}

		if (Servers.localServer.LOGINSERVER) {
			if (!exists) {
				if (!result) {
					Kingdom k = Kingdoms.getKingdomOrNull(kingdomId);
					boolean sendDelete = false;
					if (k != null) {
						if (k.isCustomKingdom()) {
							k.delete();
							Kingdoms.removeKingdom(kingdomId);
							sendDelete = true;
						}
					} else {
						sendDelete = true;
					}

					if (sendDelete) {
						WcDeleteKingdom wck = new WcDeleteKingdom(WurmId.getNextWCCommandId(), kingdomId);
						wck.encode();
						Servers.sendWebCommandToAllServers((short) 8, wck, wck.isEpicOnly());
					}
				} else {
					Servers.sendKingdomExistsToAllServers(serverId, kingdomId, false);
				}
			} else {
				Servers.sendKingdomExistsToAllServers(serverId, kingdomId, true);
			}
		}

		return result;
	}

	public static void main(String[] args) {
		WebInterfaceTest ex;
		if (args.length == 0) {
			try {
				ex = new WebInterfaceTest();
				ex.shutdownAll("Maintenance restart. Up to thirty minutes downtime.", 600);
			} catch (Exception var3) {
				var3.printStackTrace();
			}
		} else {
			try {
				ex = new WebInterfaceTest();
				System.out.println("One");
				ex.shutDown(args[0]);
			} catch (Exception var2) {
				logger.log(Level.INFO, "failed to shut down localhost");
			}
		}

	}

	public void requestDemigod(String intraServerPassword, byte existingDeity, String existingDeityName)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Player[] players = Players.getInstance().getPlayers();

		for (int x = 0; x < players.length; ++x) {
			if (players[x].getKingdomTemplateId() == Deities.getFavoredKingdom(existingDeity)
					&& (players[x].getPower() == 0 || Servers.localServer.testServer)) {
				MissionPerformer mp = MissionPerformed.getMissionPerformer(players[x].getWurmId());
				if (mp != null) {
					MissionPerformed[] perfs = mp.getAllMissionsPerformed();
					int numsForDeity = 0;
					logger.log(Level.INFO, "Checking if " + players[x].getName() + " can be elevated.");
					MissionPerformed[] var12 = perfs;
					int var11 = perfs.length;

					for (int var10 = 0; var10 < var11; ++var10) {
						MissionPerformed asc = var12[var10];
						Mission m = asc.getMission();
						if (m != null) {
							logger.log(Level.INFO, "Found a mission for " + existingDeityName);
							if (m.getCreatorType() == 2 && m.getOwnerId() == (long) existingDeity) {
								++numsForDeity;
							}
						}
					}

					logger.log(Level.INFO, "Found " + numsForDeity + " missions for " + players[x].getName());
					if (Server.rand.nextInt(numsForDeity) > 2) {
						logger.log(Level.INFO, "Sending ascension to " + players[x].getName());
						AscensionQuestion var14 = new AscensionQuestion(players[x], (long) existingDeity,
								existingDeityName);
						var14.sendQuestion();
					}
				}
			}
		}

	}

	public String ascend(String intraServerPassword, int newId, String deityName, long wurmid, byte existingDeity,
			byte gender, byte newPower, float initialAttack, float initialVitality) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		String toReturn = "";
		String sposs2;
		if (Servers.localServer.LOGINSERVER) {
			Deity sgender = null;
			if (newPower == 2) {
				sgender = Deities.ascend(newId, deityName, wurmid, gender, newPower, initialAttack, initialVitality);
				if (sgender == null) {
					return "Ouch, failed to save your demigod on the login server. Please contact administration";
				}

				StringBuilder sposs = new StringBuilder("You have now ascended! ");
				if (initialAttack < 2.0F) {
					sposs.append("The other immortals will not fear your strength initially. ");
				} else if (initialAttack < 5.0F) {
					sposs.append("You have acceptable strength as a demigod. ");
				} else if (initialAttack < 8.0F) {
					sposs.append("Your strength and skills will impress other immortals. ");
				} else {
					sposs.append("Your enormous strength will strike fear in other immortals. ");
				}

				if (initialVitality < 2.0F) {
					sposs.append(
							"You are not the most vital demigod around so you will have to watch your back in the beginning. ");
				} else if (initialVitality < 5.0F) {
					sposs.append("Your vitality is acceptable and will earn respect. ");
				} else if (initialVitality < 8.0F) {
					sposs.append("You have good vitality and can expect a bright future as immortal. ");
				} else {
					sposs.append(
							"Other immortals will envy your fantastic vitality and avoid confrontations with you. ");
				}

				if (sgender.healer) {
					sposs.append("Your love and kindness will be a beacon for everyone to follow. ");
				} else if (sgender.hateGod) {
					sposs.append("Your true nature turns out to be based on rage and hate. ");
				}

				if (sgender.forestGod) {
					sposs.append("Love for trees and living things will bind your followers together. ");
				}

				if (sgender.mountainGod) {
					sposs.append(
							"Your followers will look for you in high places and fear and adore you as they do the dragon. ");
				}

				if (sgender.waterGod) {
					sposs.append("You will be considered the pathfinder and explorer of your kin. ");
				}

				HexMap.VALREI.addDemigod(deityName, (long) sgender.number, (long) existingDeity, initialAttack,
						initialVitality);
				toReturn = sposs.toString();
			} else if (newPower > 2) {
				sposs2 = "He";
				String sposs1 = "his";
				if (gender == 1) {
					sposs2 = "She";
					sposs1 = "her";
				}

				Servers.ascend(newId, deityName, wurmid, existingDeity, gender, newPower, initialAttack,
						initialVitality);
				HistoryManager.addHistory(deityName,
						"has joined the ranks of true deities. " + sposs2 + " invites you to join " + sposs1
								+ " religion, as " + sposs2.toLowerCase()
								+ " will now forever partake in the hunts on Valrei!");
				Server.getInstance()
						.broadCastSafe(deityName + " has joined the ranks of true deities. " + sposs2
								+ " invites you to join " + sposs1 + " religion, as " + sposs2.toLowerCase()
								+ " will now forever partake in the hunts on Valrei!");
			}
		} else if (newPower > 2) {
			Deities.ascend(newId, deityName, wurmid, gender, newPower, initialAttack, initialVitality);
			String sgender1 = "He";
			sposs2 = "his";
			if (gender == 1) {
				sgender1 = "She";
				sposs2 = "her";
			}

			HistoryManager.addHistory(deityName,
					"has joined the ranks of true deities. " + sgender1 + " invites you to join " + sposs2
							+ " religion, as " + sgender1.toLowerCase()
							+ " will now forever partake in the hunts on Valrei!");
			Server.getInstance()
					.broadCastSafe(deityName + " has joined the ranks of true deities. " + sgender1
							+ " invites you to join " + sposs2 + " religion, as " + sgender1.toLowerCase()
							+ " will now forever partake in the hunts on Valrei!");
		}

		return toReturn;
	}

	public final int[] getPremTimeSilvers(String intraServerPassword, long wurmId) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
		if (info != null) {
			try {
				if (!info.loaded) {
					info.load();
				}

				if (info.getPaymentExpire() > 0L && info.awards != null) {
					int[] toReturn = new int[] { info.awards.getMonthsPaidEver(), info.awards.getSilversPaidEver() };
					return toReturn;
				}
			} catch (IOException var6) {
				;
			}
		}

		return emptyIntZero;
	}

	public void awardPlayer(String intraServerPassword, long wurmid, String name, int days, int months)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Server.addPendingAward(new PendingAward(wurmid, name, days, months));
	}

	public boolean requestDeityMove(String intraServerPassword, int deityNum, int desiredHex, String guide)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		if (Servers.localServer.LOGINSERVER) {
			EpicEntity entity = HexMap.VALREI.getEntity((long) deityNum);
			if (entity != null) {
				logger.log(Level.INFO, "Requesting move for " + entity);
				MapHex mh = HexMap.VALREI.getMapHex(desiredHex);
				if (mh != null) {
					entity.setNextTargetHex(desiredHex);
					entity.broadCastWithName(" was guided by " + guide + " towards " + mh.getName() + ".");
					entity.sendEntityData();
					return true;
				}

				logger.log(Level.INFO, "No hex for " + desiredHex);
			} else {
				logger.log(Level.INFO, "Requesting move for nonexistant " + deityNum);
			}
		}

		return false;
	}

	private void validateIntraServerPassword(String intraServerPassword) throws AccessException {
		if (!Servers.localServer.INTRASERVERPASSWORD.equals(intraServerPassword)) {
			throw new AccessException("Access denied.");
		}
	}

	public boolean wuaBan(String intraServerPassword, final String name, String ip, final String reason, final int days)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Player toBan;

		try {
			toBan = Players.getInstance().getPlayer(name);

			this.ban(intraServerPassword, name, reason, days);
			this.addBannedIp(intraServerPassword, ip, reason, days);

			if (toBan.hasLink()) {
				toBan.getCommunicator().sendAlertServerMessage(
						"You have been banned for " + days + " days and thrown out from the game.");
				toBan.logoutIn(5, "You have been banned for " + days + ". Reason: " + reason);
			}

			return true;
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean wuaMute(String intraServerPassword, final String name, final String reason, final int hours)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
		Player toMute;

		if (info != null) {
			final long expiry = System.currentTimeMillis() + hours * 3600000L;
			try {
				info.load();
			} catch (IOException iox) {
				WebInterfaceImpl.logger.log(Level.WARNING,
						"Failed to load the player information. Not muted - " + iox.getMessage(), iox);
				return false;
			}
			try {
				info.setMuted(true, reason, expiry);
				toMute = Players.getInstance().getPlayer(name);
				toMute.getCommunicator()
						.sendAlertServerMessage("You have been muted for " + hours + " hours. Reason: " + reason);

			} catch (NoSuchPlayerException iox) {
				WebInterfaceImpl.logger.log(Level.WARNING,
						"Failed to save the player information. Not muted - " + iox.getMessage(), iox);
				return false;
			}
			WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Login server muted " + name
					+ ": " + reason + " for " + hours + " hours.");
			return true;
		}
		WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
		return false;
	}

	public boolean wuaUnMute(String intraServerPassword, final String name) throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
		Player toMute;

		if (info != null) {
			try {
				info.load();
			} catch (IOException iox) {
				WebInterfaceImpl.logger.log(Level.WARNING,
						"Failed to load the player information. Not unmuted - " + iox.getMessage(), iox);
				return false;
			}
			try {
				info.setMuted(false, "", 0);
				toMute = Players.getInstance().getPlayer(name);
				toMute.getCommunicator().sendAlertServerMessage("You have been unmuted!");

			} catch (NoSuchPlayerException iox) {
				WebInterfaceImpl.logger.log(Level.WARNING,
						"Failed to save the player information. Not unmuted - " + iox.getMessage(), iox);
				return false;
			}
			WebInterfaceImpl.logger
					.info(String.valueOf(this.getRemoteClientDetails()) + " Login server unmuted " + name);
			return true;
		}
		WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
		return false;
	}

	public boolean wuaChangePower(String intraServerPassword, final String name, final int gmLevel)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Player player;

		try {
			player = Players.getInstance().getPlayer(name);
			player.setPower((byte) gmLevel);
			player.save();
			String power = "";
			switch (gmLevel) {
			case 0:
				power = "Player";
				break;
			case 1:
				power = "HERO";
				break;
			case 2:
				power = "GM";
				break;
			case 3:
				power = "High God";
				break;
			case 4:
				power = "Arch GM";
				break;
			case 5:
				power = "Implementor";
				break;
			}
			player.getCommunicator().sendAlertServerMessage("Your powers were changed! You are now a " + power);

		} catch (NoSuchPlayerException | IOException iox) {
			WebInterfaceImpl.logger.log(Level.WARNING,
					"Failed to save the player information. Power not changed - " + iox.getMessage(), iox);
			return false;
		}

		WebInterfaceImpl.logger
				.info(String.valueOf(this.getRemoteClientDetails()) + " Changed powers of " + name + " to " + gmLevel);
		return true;
	}

	public boolean wuaChangeKingdom(String intraServerPassword, final String name, final int kingdom)
			throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Player player;

		try {
			player = Players.getInstance().getPlayer(name);
			player.setCurrentKingdom(((byte) kingdom));
			player.save();
			String[] kingdoms = { "No kingdom", "Jenn-Kellon", "Mol-Rehan", "Horde of the Summoned", "Freedom Isles" };
			player.getCommunicator()
					.sendAlertServerMessage("Your kingdom was changed! You are now part of " + kingdoms[kingdom]);
			WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Changed kingdom of " + name
					+ " to " + kingdoms[kingdom]);
			return true;
		} catch (NoSuchPlayerException | IOException iox) {
			WebInterfaceImpl.logger.log(Level.WARNING,
					"Failed to save the player information. Kingdom not changed - " + iox.getMessage(), iox);
			return false;
		}
	}

	public boolean wuaGiveItem(String intraServerPassword, final String name, final int itemTemplateID,
			final float itemQuality, final byte itemRarity, final String creator, final int itemAmount)
					throws RemoteException {
		this.validateIntraServerPassword(intraServerPassword);
		Player player;
		ItemFactory itemFactory = null;
		Item tempItem = null;

		try {
			player = Players.getInstance().getPlayer(name);
			for (int i = 0; i < itemAmount; i++) {
				tempItem = itemFactory.createItem(itemTemplateID, itemQuality, itemRarity, creator);
				player.getInventory().insertItem(tempItem);
			}
			player.getCommunicator()
					.sendAlertServerMessage(itemAmount + "x of " + tempItem.getName() + " was added to your inventory");
		} catch (NoSuchPlayerException | FailedException | NoSuchTemplateException iox) {
			//WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Item not added - " + iox.getMessage(), iox);
			return false;
		}
		WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Added item");
		return true;

	}
}
