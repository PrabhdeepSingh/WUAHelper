package com.wurmonline.server.webinterface;

import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.behaviours.VehicleSettings;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.Features;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.NoSuchTemplateException;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillStat;
import java.util.regex.Matcher;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.questions.WurmInfo2;
import com.wurmonline.server.questions.WurmInfo;
import com.wurmonline.server.questions.NewsInfo;
import com.wurmonline.server.players.BannedIp;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.Items;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.Message;
import java.math.BigInteger;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.security.MessageDigest;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.LoginServerWebConnection;
import java.net.URLEncoder;
import com.wurmonline.server.Mailer;
import java.util.Iterator;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.villages.Citizen;
import java.util.Date;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Server;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.NoSuchPlayerException;
import java.io.IOException;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.Players;
import java.rmi.server.ServerNotActiveException;
import java.util.logging.Level;
import java.rmi.server.RemoteServer;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.Set;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Pattern;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.CounterTypes;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.MiscConstants;
import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;

public final class WebInterfaceImpl extends UnicastRemoteObject implements WebInterface, Serializable, MiscConstants, TimeConstants, CounterTypes, MonetaryConstants
{
    public static final String VERSION = "$Revision: 1.54 $";
    public static String mailAccount;
    public static final Pattern VALID_EMAIL_PATTERN;
    private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
    private static final long serialVersionUID = -2682536434841429586L;
    private final boolean isRunning = true;
    private final Random faceRandom;
    private static final long faceRandomSeed = 8263186381637L;
    private static final DecimalFormat twoDecimals;
    private static final Set<String> moneyDetails;
    private static final Set<String> timeDetails;
    private static final Logger logger;
    private static final long[] noInfoLong;
    private final SimpleDateFormat alloformatter;
    private String hostname;
    private static final Map<String, Long> ipAttempts;
    private String[] bannedMailHosts;
    static final int[] emptyIntZero;
    
    static {
        WebInterfaceImpl.mailAccount = "mail@mydomain.com";
        VALID_EMAIL_PATTERN = Pattern.compile("^[\\w\\.\\+-=]+@[\\w\\.-]+\\.[\\w-]+$");
        twoDecimals = new DecimalFormat("##0.00");
        moneyDetails = new HashSet<String>();
        timeDetails = new HashSet<String>();
        logger = Logger.getLogger(WebInterfaceImpl.class.getName());
        noInfoLong = new long[] { -1L, -1L };
        ipAttempts = new HashMap<String, Long>();
        emptyIntZero = new int[2];
    }
    
    public WebInterfaceImpl(final int port) throws RemoteException {
        super(port);
        this.faceRandom = new Random();
        this.alloformatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
        this.hostname = "localhost";
        this.bannedMailHosts = new String[] { "sharklasers", "spam4", "grr.la", "guerrillamail" };
        try {
            final InetAddress localMachine = InetAddress.getLocalHost();
            this.hostname = localMachine.getHostName();
            WebInterfaceImpl.logger.info("Hostname of local machine used to send registration emails: " + this.hostname);
        }
        catch (UnknownHostException uhe) {
            throw new RemoteException("Could not find localhost for WebInterface", uhe);
        }
    }
    
    public WebInterfaceImpl() throws RemoteException {
        this.faceRandom = new Random();
        this.alloformatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
        this.hostname = "localhost";
        this.bannedMailHosts = new String[] { "sharklasers", "spam4", "grr.la", "guerrillamail" };
    }
    
    private String getRemoteClientDetails() {
        try {
            return RemoteServer.getClientHost();
        }
        catch (ServerNotActiveException e) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Could not get ClientHost details due to " + e.getMessage(), e);
            return "Unknown Remote Client";
        }
    }
    
    @Override
    public int getPower(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPower for playerID: " + aPlayerID);
        }
        try {
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
            p.load();
            return p.getPower();
        }
        catch (IOException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(aPlayerID) + ": " + iox.getMessage(), iox);
            return 0;
        }
        catch (NoSuchPlayerException ex) {
            return 0;
        }
    }
    
    @Override
    public boolean isRunning() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " isRunning");
        }
        return true;
    }
    
    @Override
    public int getPlayerCount() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayerCount");
        }
        return Players.getInstance().numberOfPlayers();
    }
    
    @Override
    public int getPremiumPlayerCount() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPremiumPlayerCount");
        }
        return Players.getInstance().numberOfPremiumPlayers();
    }
    
    @Override
    public String getTestMessage() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getTestMessage");
        }
        synchronized (Server.SYNC_LOCK) {
            // monitorexit(Server.SYNC_LOCK)
            return "HEj! " + System.currentTimeMillis();
        }
    }
    
    @Override
    public void broadcastMessage(final String message) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " broadcastMessage: " + message);
        }
        synchronized (Server.SYNC_LOCK) {
            Server.getInstance().broadCastAlert(message);
        }
        // monitorexit(Server.SYNC_LOCK)
    }
    
    @Override
    public long getAccountStatusForPlayer(final String playerName) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getAccountStatusForPlayer for player: " + playerName);
        }
        synchronized (Server.SYNC_LOCK) {
            if (Servers.localServer.id != Servers.loginServer.id) {
                throw new RemoteException("Not a valid request for this server. Ask the login server instead.");
            }
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                // monitorexit(Server.SYNC_LOCK)
                return p.money;
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(playerName) + ": " + iox.getMessage(), iox);
                // monitorexit(Server.SYNC_LOCK)
                return 0L;
            }
        }
    }
    
    @Override
    public Map<String, Integer> getBattleRanks(final int numberOfRanksToGet) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getBattleRanks number of Ranks: " + numberOfRanksToGet);
        }
        synchronized (Server.SYNC_LOCK) {
            // monitorexit(Server.SYNC_LOCK)
            return Players.getBattleRanks(numberOfRanksToGet);
        }
    }
    
    @Override
    public String getServerStatus() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getServerStatus");
        }
        synchronized (Server.SYNC_LOCK) {
            String toReturn = "Up and running.";
            if (Server.getMillisToShutDown() > -1000L) {
                toReturn = "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds: " + Server.getShutdownReason();
            }
            else if (Constants.maintaining) {
                toReturn = "The server is in maintenance mode and not open for connections.";
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public Map<String, Long> getFriends(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getFriends for playerid: " + aPlayerID);
        }
        synchronized (Server.SYNC_LOCK) {
            // monitorexit(Server.SYNC_LOCK)
            return Players.getFriends(aPlayerID);
        }
    }
    
    @Override
    public Map<String, String> getInventory(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getInventory for playerid: " + aPlayerID);
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<String, String> toReturn = new HashMap<String, String>();
            try {
                final Player p = Players.getInstance().getPlayer(aPlayerID);
                final Item inventory = p.getInventory();
                final Item[] items = inventory.getAllItems(false);
                for (int x = 0; x < items.length; ++x) {
                    toReturn.put(String.valueOf(items[x].getWurmId()), String.valueOf(items[x].getName()) + ", QL: " + items[x].getQualityLevel() + ", DAM: " + items[x].getDamage());
                }
            }
            catch (NoSuchPlayerException ex) {}
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public Map<Long, Long> getBodyItems(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getBodyItems for playerid: " + aPlayerID);
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<Long, Long> toReturn = new HashMap<Long, Long>();
            try {
                final Player p = Players.getInstance().getPlayer(aPlayerID);
                final Body lBody = p.getBody();
                if (lBody != null) {
                    final Item[] items = lBody.getAllItems();
                    for (int x = 0; x < items.length; ++x) {
                        toReturn.put(items[x].getWurmId(), items[x].getParentId());
                    }
                }
            }
            catch (NoSuchPlayerException ex) {}
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public Map<String, Float> getSkills(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getSkills for playerid: " + aPlayerID);
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<String, Float> toReturn = new HashMap<String, Float>();
            final Skills skills = SkillsFactory.createSkills(aPlayerID);
            try {
                skills.load();
                final Skill[] skillarr = skills.getSkills();
                for (int x = 0; x < skillarr.length; ++x) {
                    toReturn.put(skillarr[x].getName(), new Float(skillarr[x].getKnowledge(0.0)));
                }
            }
            catch (Exception iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(aPlayerID) + ": " + iox.getMessage(), iox);
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public Map<String, ?> getPlayerSummary(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayerSummary for playerid: " + aPlayerID);
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<String, Object> toReturn = new HashMap<String, Object>();
            Label_1524: {
                if (WurmId.getType(aPlayerID) == 0) {
                    try {
                        final Player p = Players.getInstance().getPlayer(aPlayerID);
                        toReturn.put("Name", p.getName());
                        if (p.citizenVillage != null) {
                            final Citizen citiz = p.citizenVillage.getCitizen(aPlayerID);
                            toReturn.put("CitizenVillage", p.citizenVillage.getName());
                            toReturn.put("CitizenRole", citiz.getRole().getName());
                        }
                        String location = "unknown";
                        if (p.currentVillage != null) {
                            location = String.valueOf(p.currentVillage.getName()) + ", in " + Kingdoms.getNameFor(p.currentVillage.kingdom);
                        }
                        else if (p.currentKingdom != 0) {
                            location = Kingdoms.getNameFor(p.currentKingdom);
                        }
                        toReturn.put("Location", location);
                        if (p.getDeity() != null) {
                            toReturn.put("Deity", p.getDeity().name);
                        }
                        toReturn.put("Faith", new Float(p.getFaith()));
                        toReturn.put("Favor", new Float(p.getFavor()));
                        toReturn.put("Gender", p.getSex());
                        toReturn.put("Alignment", new Float(p.getAlignment()));
                        toReturn.put("Kingdom", p.getKingdomId());
                        toReturn.put("Battle rank", p.getRank());
                        toReturn.put("WurmId", new Long(aPlayerID));
                        toReturn.put("Banned", p.getSaveFile().isBanned());
                        toReturn.put("Money in bank", p.getMoney());
                        toReturn.put("Payment", new Date(p.getPaymentExpire()));
                        toReturn.put("Email", p.getSaveFile().emailAddress);
                        toReturn.put("Current server", Servers.localServer.id);
                        toReturn.put("Last login", new Date(p.getLastLogin()));
                        toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
                        if (p.getSaveFile().isBanned()) {
                            toReturn.put("Ban reason", p.getSaveFile().banreason);
                            toReturn.put("Ban expires in", Server.getTimeFor(p.getSaveFile().banexpiry - System.currentTimeMillis()));
                        }
                        toReturn.put("Warnings", String.valueOf(p.getSaveFile().getWarnings()));
                        if (p.isMute()) {
                            toReturn.put("Muted", Boolean.TRUE);
                            toReturn.put("Mute reason", p.getSaveFile().mutereason);
                            toReturn.put("Mute expires in", Server.getTimeFor(p.getSaveFile().muteexpiry - System.currentTimeMillis()));
                        }
                        toReturn.put("PlayingTime", Server.getTimeFor(p.getSaveFile().playingTime));
                        toReturn.put("Reputation", p.getReputation());
                        if (p.getTitle() != null) {
                            toReturn.put("Title", p.getTitle().getName(p.isNotFemale()));
                        }
                        toReturn.put("Coord x", (int)p.getStatus().getPositionX() >> 2);
                        toReturn.put("Coord y", (int)p.getStatus().getPositionY() >> 2);
                        if (p.isPriest()) {
                            toReturn.put("Priest", Boolean.TRUE);
                        }
                        toReturn.put("LoggedOut", p.loggedout);
                        break Label_1524;
                    }
                    catch (NoSuchPlayerException nsp3) {
                        try {
                            final PlayerInfo p2 = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
                            p2.load();
                            toReturn.put("Name", p2.getName());
                            if (p2.getDeity() != null) {
                                toReturn.put("Deity", p2.getDeity().name);
                            }
                            toReturn.put("Faith", new Float(p2.getFaith()));
                            toReturn.put("Favor", new Float(p2.getFavor()));
                            toReturn.put("Current server", p2.currentServer);
                            toReturn.put("Alignment", new Float(p2.getAlignment()));
                            toReturn.put("Battle rank", p2.getRank());
                            toReturn.put("WurmId", new Long(aPlayerID));
                            toReturn.put("Banned", p2.isBanned());
                            toReturn.put("Money in bank", new Long(p2.money));
                            toReturn.put("Payment", new Date(p2.getPaymentExpire()));
                            toReturn.put("Email", p2.emailAddress);
                            toReturn.put("Last login", new Date(p2.getLastLogin()));
                            toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
                            if (p2.isBanned()) {
                                toReturn.put("Ban reason", p2.banreason);
                                toReturn.put("Ban expires in", Server.getTimeFor(p2.banexpiry - System.currentTimeMillis()));
                            }
                            toReturn.put("Warnings", String.valueOf(p2.getWarnings()));
                            if (p2.isMute()) {
                                toReturn.put("Muted", Boolean.TRUE);
                                toReturn.put("Mute reason", p2.mutereason);
                                toReturn.put("Mute expires in", Server.getTimeFor(p2.muteexpiry - System.currentTimeMillis()));
                            }
                            toReturn.put("PlayingTime", Server.getTimeFor(p2.playingTime));
                            toReturn.put("Reputation", p2.getReputation());
                            if (p2.title != null && p2.title.getName(true) != null) {
                                toReturn.put("Title", p2.title.getName(true));
                            }
                            if (p2.isPriest) {
                                toReturn.put("Priest", Boolean.TRUE);
                            }
                            break Label_1524;
                        }
                        catch (IOException iox) {
                            WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(aPlayerID) + ":" + iox.getMessage(), iox);
                        }
                        catch (NoSuchPlayerException nsp2) {
                            WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(aPlayerID) + ":" + nsp2.getMessage(), (Throwable)nsp2);
                        }
                    }
                }
                toReturn.put("Not a player", String.valueOf(aPlayerID));
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public long getLocalCreationTime() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getLocalCreationTime");
        }
        return Server.getStartTime();
    }
    
    @Override
    public Map<Integer, String> getKingdoms() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getKingdoms");
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<Integer, String> toReturn = new HashMap<Integer, String>();
            if (Servers.localServer.HOMESERVER) {
                toReturn.put((int)Servers.localServer.KINGDOM, Kingdoms.getNameFor(Servers.localServer.KINGDOM));
            }
            else {
                toReturn.put(1, Kingdoms.getNameFor((byte)1));
                toReturn.put(3, Kingdoms.getNameFor((byte)3));
                toReturn.put(2, Kingdoms.getNameFor((byte)2));
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public Map<Long, String> getPlayersForKingdom(final int aKingdom) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayersForKingdom: " + aKingdom);
        }
        synchronized (Server.SYNC_LOCK) {
            final Map<Long, String> toReturn = new HashMap<Long, String>();
            final Player[] players = Players.getInstance().getPlayers();
            for (int x = 0; x < players.length; ++x) {
                if (players[x].getKingdomId() == aKingdom) {
                    toReturn.put(new Long(players[x].getWurmId()), players[x].getName());
                }
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public long getPlayerId(final String name) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayerId for player name: " + name);
        }
        synchronized (Server.SYNC_LOCK) {
            // monitorexit(Server.SYNC_LOCK)
            return Players.getInstance().getWurmIdByPlayerName(LoginHandler.raiseFirstLetter(name));
        }
    }
    
    @Override
    public Map<String, ?> createPlayer(final String name, final String password, final String challengePhrase, final String challengeAnswer, final String emailAddress, final byte kingdom, final byte power, long appearance, final byte gender) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " createPlayer for player name: " + name);
        }
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        WebInterfaceImpl.logger.log(Level.INFO, "Trying to create player " + name);
        synchronized (Server.SYNC_LOCK) {
            if (isEmailValid(emailAddress)) {
                try {
                    toReturn.put("PlayerId", new Long(LoginHandler.createPlayer(name, password, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
                }
                catch (Exception ex) {
                    toReturn.put("PlayerId", -1L);
                    toReturn.put("error", ex.getMessage());
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + ex.getMessage(), ex);
                }
            }
            else {
                toReturn.put("error", "The email address " + emailAddress + " is not valid.");
            }
        }
        // monitorexit(Server.SYNC_LOCK)
        return toReturn;
    }
    
    @Override
    public Map<String, String> getPendingAccounts() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPendingAccounts");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        for (final Map.Entry<String, PendingAccount> entry : PendingAccount.accounts.entrySet()) {
            toReturn.put(entry.getKey(), String.valueOf(entry.getValue().emailAddress) + ", " + GeneralUtilities.toGMTString(entry.getValue().expiration));
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> createPlayerPhaseOne(String aPlayerName, final String aEmailAddress) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("error", "The server is currently in maintenance mode.");
            return toReturn;
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Trying to create player phase one " + aPlayerName);
        synchronized (Server.SYNC_LOCK) {
            aPlayerName = LoginHandler.raiseFirstLetter(aPlayerName);
            final String errstat = LoginHandler.checkName2(aPlayerName);
            if (errstat.length() == 0) {
                if (PlayerInfoFactory.doesPlayerExist(aPlayerName)) {
                    toReturn.put("error", "The name " + aPlayerName + " is taken.");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                if (PendingAccount.doesPlayerExist(aPlayerName)) {
                    toReturn.put("error", "The name " + aPlayerName + " is reserved for up to two days.");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                if (!isEmailValid(aEmailAddress)) {
                    toReturn.put("error", "The email " + aEmailAddress + " is invalid.");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                final String[] numAccounts = PlayerInfoFactory.getAccountsForEmail(aEmailAddress);
                if (numAccounts.length >= 5) {
                    String accnames = "";
                    for (int x = 0; x < numAccounts.length; ++x) {
                        accnames = String.valueOf(accnames) + " " + numAccounts[x];
                    }
                    toReturn.put("error", "You may only have 5 accounts. Please play Wurm with any of the following:" + accnames + ".");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                final String[] numAccounts2 = PendingAccount.getAccountsForEmail(aEmailAddress);
                if (numAccounts2.length >= 5) {
                    String accnames2 = "";
                    for (int x2 = 0; x2 < numAccounts2.length; ++x2) {
                        accnames2 = String.valueOf(accnames2) + " " + numAccounts2[x2];
                    }
                    toReturn.put("error", "You may only have 5 accounts. The following accounts are awaiting confirmation by following the link in the verification email:" + accnames2 + ".");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                String[] bannedMailHosts;
                for (int length = (bannedMailHosts = this.bannedMailHosts).length, i = 0; i < length; ++i) {
                    final String blocked = bannedMailHosts[i];
                    if (aEmailAddress.toLowerCase().contains(blocked)) {
                        final String domain = aEmailAddress.substring(aEmailAddress.indexOf("@"), aEmailAddress.length());
                        toReturn.put("error", "We do not accept email addresses from :" + domain + ".");
                        // monitorexit(Server.SYNC_LOCK)
                        return toReturn;
                    }
                }
                if (numAccounts.length + numAccounts2.length >= 5) {
                    String accnames2 = "";
                    for (int x2 = 0; x2 < numAccounts.length; ++x2) {
                        accnames2 = String.valueOf(accnames2) + " " + numAccounts[x2];
                    }
                    for (int x2 = 0; x2 < numAccounts2.length; ++x2) {
                        accnames2 = String.valueOf(accnames2) + " " + numAccounts2[x2];
                    }
                    toReturn.put("error", "You may only have 5 accounts. The following accounts are already registered or awaiting confirmation by following the link in the verification email:" + accnames2 + ".");
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                final String password = generateRandomPassword();
                final long expireTime = System.currentTimeMillis() + 172800000L;
                final PendingAccount pedd = new PendingAccount();
                pedd.accountName = aPlayerName;
                pedd.emailAddress = aEmailAddress;
                pedd.expiration = expireTime;
                pedd.password = password;
                if (pedd.create()) {
                    try {
                        if (!Constants.devmode) {
                            String email = Mailer.getPhaseOneMail();
                            email = email.replace("@pname", aPlayerName);
                            email = email.replace("@email", URLEncoder.encode(aEmailAddress, "UTF-8"));
                            email = email.replace("@expiration", GeneralUtilities.toGMTString(expireTime));
                            email = email.replace("@password", password);
                            Mailer.sendMail(WebInterfaceImpl.mailAccount, aEmailAddress, "Wurm Online character creation request", email);
                        }
                        else {
                            toReturn.put("Hash", password);
                            WebInterfaceImpl.logger.log(Level.WARNING, "NO MAIL SENT: DEVMODE ACTIVE");
                        }
                        toReturn.put("ok", "An email has been sent to " + aEmailAddress + ". You will have to click a link in order to proceed with the registration.");
                    }
                    catch (Exception ex) {
                        toReturn.put("error", "An error occured when sending the mail: " + ex.getMessage() + ". No account was reserved.");
                        pedd.delete();
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(aEmailAddress) + ":" + ex.getMessage(), ex);
                    }
                }
                else {
                    toReturn.put("error", "The account could not be created. Please try later.");
                    WebInterfaceImpl.logger.warning(String.valueOf(aEmailAddress) + " The account could not be created. Please try later.");
                }
            }
            else {
                toReturn.put("error", errstat);
            }
        }
        // monitorexit(Server.SYNC_LOCK)
        return toReturn;
    }
    
    @Override
    public Map<String, ?> createPlayerPhaseTwo(final String playerName, final String hashedIngamePassword, final String challengePhrase, final String challengeAnswer, final String emailAddress, final byte kingdom, final byte power, long appearance, final byte gender, final String phaseOneHash) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " createPlayerPhaseTwo for player name: " + playerName);
        }
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        return this.createPlayerPhaseTwo(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, 1);
    }
    
    @Override
    public Map<String, ?> createPlayerPhaseTwo(final String playerName, final String hashedIngamePassword, final String challengePhrase, final String challengeAnswer, final String emailAddress, final byte kingdom, final byte power, long appearance, final byte gender, final String phaseOneHash, final int serverId) throws RemoteException {
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        return this.createPlayerPhaseTwo(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, serverId, true);
    }
    
    @Override
    public Map<String, ?> createPlayerPhaseTwo(String playerName, final String hashedIngamePassword, final String challengePhrase, final String challengeAnswer, final String emailAddress, byte kingdom, final byte power, long appearance, final byte gender, final String phaseOneHash, int serverId, final boolean optInEmail) throws RemoteException {
        serverId = 1;
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        kingdom = 4;
        if (kingdom == 3) {
            serverId = 3;
        }
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        if (Constants.maintaining) {
            toReturn.put("error", "The server is currently in maintenance mode.");
            return toReturn;
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Trying to create player phase two " + playerName);
        synchronized (Server.SYNC_LOCK) {
            if (playerName == null || hashedIngamePassword == null || challengePhrase == null || challengeAnswer == null || emailAddress == null || phaseOneHash == null) {
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
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (challengePhrase.equals(challengeAnswer)) {
                toReturn.put("error", "We don't allow the password retrieval question and answer to be the same.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            playerName = LoginHandler.raiseFirstLetter(playerName);
            final String errstat = LoginHandler.checkName2(playerName);
            if (errstat.length() > 0) {
                toReturn.put("error", errstat);
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (PlayerInfoFactory.doesPlayerExist(playerName)) {
                toReturn.put("error", "The name " + playerName + " is taken. Your reservation must have expired.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (hashedIngamePassword.length() < 6 || hashedIngamePassword.length() > 40) {
                toReturn.put("error", "The hashed password must contain at least 6 characters and maximum 40 characters.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (challengePhrase.length() < 4 || challengePhrase.length() > 120) {
                toReturn.put("error", "The challenge phrase must contain at least 4 characters and max 120 characters.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (challengeAnswer.length() < 1 || challengeAnswer.length() > 20) {
                toReturn.put("error", "The challenge answer must contain at least 1 character and max 20 characters.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (emailAddress.length() > 125) {
                toReturn.put("error", "The email address consists of too many characters.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            Label_1362: {
                if (isEmailValid(emailAddress)) {
                    try {
                        final PendingAccount pacc = PendingAccount.getAccount(playerName);
                        if (pacc == null) {
                            toReturn.put("PlayerId", -1L);
                            toReturn.put("error", "The verification is done too late or the name was never reserved. The name reservation expires after two days. Please try to create the player again.");
                            // monitorexit(Server.SYNC_LOCK)
                            return toReturn;
                        }
                        if (pacc.password.equals(phaseOneHash)) {
                            Label_1022: {
                                if (pacc.emailAddress.toLowerCase().equals(emailAddress.toLowerCase())) {
                                    Label_1059: {
                                        try {
                                            if (serverId == Servers.localServer.id) {
                                                toReturn.put("PlayerId", new Long(LoginHandler.createPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
                                                break Label_1059;
                                            }
                                            if (!Servers.localServer.LOGINSERVER) {
                                                toReturn.put("PlayerId", -1L);
                                                toReturn.put("error", "Failed to create player " + playerName + ": This is not a login server.");
                                                break Label_1059;
                                            }
                                            final ServerEntry toCreateOn = Servers.getServerWithId(serverId);
                                            if (toCreateOn != null) {
                                                int tilex = toCreateOn.SPAWNPOINTJENNX;
                                                int tiley = toCreateOn.SPAWNPOINTJENNY;
                                                if (kingdom == 2) {
                                                    tilex = toCreateOn.SPAWNPOINTMOLX;
                                                    tiley = toCreateOn.SPAWNPOINTMOLY;
                                                }
                                                if (kingdom == 3) {
                                                    tilex = toCreateOn.SPAWNPOINTLIBX;
                                                    tiley = toCreateOn.SPAWNPOINTLIBY;
                                                }
                                                final LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
                                                final byte[] playerData = lsw.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, false, false, false);
                                                final long wurmId = IntraServerConnection.savePlayerToDisk(playerData, tilex, tiley, true, true);
                                                toReturn.put("PlayerId", wurmId);
                                                break Label_1059;
                                            }
                                            toReturn.put("PlayerId", -1L);
                                            toReturn.put("error", "Failed to create player " + playerName + ": The desired server does not exist.");
                                            break Label_1059;
                                        }
                                        catch (Exception cex) {
                                            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + cex.getMessage(), cex);
                                            toReturn.put("PlayerId", -1L);
                                            toReturn.put("error", "Failed to create player " + playerName + ":" + cex.getMessage());
                                            // monitorexit(Server.SYNC_LOCK)
                                            return toReturn;
                                        }
                                    }
                                    pacc.delete();
                                    try {
                                        if (!Constants.devmode) {
                                            String mail = Mailer.getPhaseTwoMail();
                                            mail = mail.replace("@pname", playerName);
                                            Mailer.sendMail(WebInterfaceImpl.mailAccount, emailAddress, "Wurm Online character creation success", mail);
                                        }
                                    }
                                    catch (Exception cex2) {
                                        WebInterfaceImpl.logger.log(Level.WARNING, "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2.getMessage(), cex2);
                                        toReturn.put("error", "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2.getMessage());
                                    }
                                    break Label_1362;
                                }
                            }
                            toReturn.put("PlayerId", -1L);
                            toReturn.put("error", "The email supplied does not match with the one that was registered with the name.");
                            // monitorexit(Server.SYNC_LOCK)
                            return toReturn;
                        }
                        toReturn.put("PlayerId", -1L);
                        toReturn.put("error", "The verification hash does not match.");
                    }
                    catch (Exception ex) {
                        WebInterfaceImpl.logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + ex.getMessage(), ex);
                        toReturn.put("PlayerId", -1L);
                        toReturn.put("error", ex.getMessage());
                    }
                }
                else {
                    toReturn.put("error", "The email address " + emailAddress + " is not valid.");
                }
            }
        }
        // monitorexit(Server.SYNC_LOCK)
        return toReturn;
    }
    
    @Override
    public byte[] createAndReturnPlayer(final String playerName, final String hashedIngamePassword, final String challengePhrase, final String challengeAnswer, final String emailAddress, final byte kingdom, final byte power, long appearance, final byte gender, final boolean titleKeeper, final boolean addPremium, final boolean passwordIsHashed) throws RemoteException {
        if (Constants.maintaining) {
            throw new RemoteException("The server is currently in maintenance mode.");
        }
        try {
            appearance = Server.rand.nextInt(5);
            this.faceRandom.setSeed(8263186381637L + appearance);
            appearance = this.faceRandom.nextLong();
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(RemoteServer.getClientHost()) + " Received create attempt for " + playerName);
            return LoginHandler.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed);
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }
    
    @Override
    public Map<String, String> addMoneyToBank(String name, final long moneyToAdd, final String transactionDetail) throws RemoteException {
        final byte executor = 6;
        final boolean ok = true;
        final String campaignId = "";
        name = LoginHandler.raiseFirstLetter(name);
        final Map<String, String> toReturn = new HashMap<String, String>();
        if (name == null || name.length() == 0) {
            toReturn.put("error", "Illegal name.");
            return toReturn;
        }
        if (moneyToAdd <= 0L) {
            toReturn.put("error", "Invalid amount; must be greater than zero");
            return toReturn;
        }
        synchronized (Server.SYNC_LOCK) {
            Label_0595: {
                try {
                    final Player p = Players.getInstance().getPlayer(name);
                    p.addMoney(moneyToAdd);
                    final long money = p.getMoney();
                    new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, executor, campaignId);
                    final Change change = new Change(moneyToAdd);
                    final Change current = new Change(money);
                    p.save();
                    toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ".");
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        final PlayerInfo p2 = PlayerInfoFactory.createPlayerInfo(name);
                        p2.load();
                        if (p2.wurmId <= 0L) {
                            toReturn.put("error", "No player found with the name " + name + ".");
                            break Label_0595;
                        }
                        p2.setMoney(p2.money + moneyToAdd);
                        final Change change2 = new Change(moneyToAdd);
                        final Change current2 = new Change(p2.money);
                        p2.save();
                        toReturn.put("ok", "An amount of " + change2.getChangeString() + " has been added to the account. Current balance is " + current2.getChangeString() + ". It may take a while to reach your server.");
                        if (Servers.localServer.id != p2.currentServer) {
                            new MoneyTransfer(name, p2.wurmId, p2.money, moneyToAdd, transactionDetail, executor, campaignId, false);
                            break Label_0595;
                        }
                        new MoneyTransfer(p2.getName(), p2.wurmId, p2.money, moneyToAdd, transactionDetail, executor, campaignId);
                    }
                    catch (IOException iox) {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + iox.getMessage(), iox);
                        throw new RemoteException("An error occured. Please contact customer support.");
                    }
                }
                catch (IOException iox2) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + iox2.getMessage(), iox2);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
                catch (Exception ex) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + ex.getMessage(), ex);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
            }
        }
        // monitorexit(Server.SYNC_LOCK)
        return toReturn;
    }
    
    @Override
    public long getMoney(final long playerId, final String playerName) throws RemoteException {
        PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
        if (p == null) {
            p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load pinfo for " + playerName);
            }
            if (p.wurmId <= 0L) {
                return 0L;
            }
        }
        if (p != null) {
            return p.money;
        }
        return 0L;
    }
    
    @Override
    public Map<String, String> reversePayment(final long moneyToRemove, final int monthsToRemove, final int daysToRemove, final String reversalTransactionID, final String originalTransactionID, final String playerName) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Reverse payment for player name: " + playerName + ", reversalTransactionID: " + reversalTransactionID + ", originalTransactionID: " + originalTransactionID);
        try {
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            p.load();
            if (p.wurmId > 0L) {
                if (moneyToRemove > 0L) {
                    if (p.money < moneyToRemove) {
                        final Change lack = new Change(moneyToRemove - p.money);
                        toReturn.put("moneylack", "An amount of " + lack.getChangeString() + " was lacking from the account. Removing what we can.");
                    }
                    p.setMoney(Math.max(0L, p.money - moneyToRemove));
                    final Change change = new Change(moneyToRemove);
                    final Change current = new Change(p.money);
                    p.save();
                    toReturn.put("moneyok", "An amount of " + change.getChangeString() + " has been removed from the account. Current balance is " + current.getChangeString() + ".");
                    if (Servers.localServer.id != p.currentServer) {
                        new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "", false);
                    }
                    else {
                        new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "");
                    }
                }
                if (daysToRemove > 0 || monthsToRemove > 0) {
                    long timeToRemove = 0L;
                    if (daysToRemove > 0) {
                        timeToRemove = daysToRemove * 86400000L;
                    }
                    if (monthsToRemove > 0) {
                        timeToRemove += monthsToRemove * 86400000L * 30L;
                    }
                    long currTime = p.getPaymentExpire();
                    currTime = Math.max(currTime, System.currentTimeMillis());
                    currTime = Math.max(currTime - timeToRemove, System.currentTimeMillis());
                    try {
                        p.setPaymentExpire(currTime);
                        String expireString = "The premier playing time has expired now.";
                        if (System.currentTimeMillis() < currTime) {
                            expireString = "The player now has premier playing time until " + GeneralUtilities.toGMTString(currTime) + ". Your in game player account will be updated shortly.";
                        }
                        p.save();
                        toReturn.put("timeok", expireString);
                        if (p.currentServer != Servers.localServer.id) {
                            new TimeTransfer(playerName, p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID, false);
                        }
                        else {
                            new TimeTransfer(p.getName(), p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID);
                        }
                    }
                    catch (IOException iox) {
                        toReturn.put("timeerror", String.valueOf(p.getName()) + ": failed to set expire to " + currTime + ", " + iox.getMessage());
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(p.getName()) + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
                    }
                }
            }
            else {
                toReturn.put("error", "No player found with the name " + playerName + ".");
            }
        }
        catch (IOException iox2) {
            WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(playerName) + ":" + iox2.getMessage(), iox2);
            throw new RemoteException("An error occured. Please contact customer support.");
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> addMoneyToBank(final String name, final long moneyToAdd, final String transactionDetail, final boolean ingame) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " addMoneyToBank for player name: " + name);
        }
        return this.addMoneyToBank(name, -1L, moneyToAdd, transactionDetail, ingame);
    }
    
    public static String encryptMD5(final String plaintext) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WurmServerException("No such algorithm 'MD5'", (Throwable)e);
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e2) {
            throw new WurmServerException("No such encoding: UTF-8", (Throwable)e2);
        }
        final byte[] raw = md.digest();
        final BigInteger bi = new BigInteger(1, raw);
        final String hash = bi.toString(16);
        return hash;
    }
    
    @Override
    public Map<String, String> addMoneyToBank(String name, final long wurmId, final long moneyToAdd, final String transactionDetail, final boolean ingame) throws RemoteException {
        synchronized (Server.SYNC_LOCK) {
            final Map<String, String> toReturn = new HashMap<String, String>();
            if ((name == null || name.length() == 0) && wurmId <= 0L) {
                toReturn.put("error", "Illegal name.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (moneyToAdd <= 0L) {
                toReturn.put("error", "Invalid amount; must be greater than zero");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (name != null) {
                name = LoginHandler.raiseFirstLetter(name);
            }
            final byte executor = 6;
            final String campaignId = "";
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Add money to bank 2 , " + moneyToAdd + " for player name: " + name + ", wid " + wurmId);
            Label_0827: {
                if (name == null || name.length() <= 0) {
                    if (wurmId <= 0L) {
                        break Label_0827;
                    }
                }
                try {
                    Player p = null;
                    if (wurmId <= 0L) {
                        p = Players.getInstance().getPlayer(name);
                    }
                    else {
                        p = Players.getInstance().getPlayer(wurmId);
                    }
                    p.addMoney(moneyToAdd);
                    final long money = p.getMoney();
                    if (!ingame) {
                        new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, (byte)6, "");
                    }
                    final Change change = new Change(moneyToAdd);
                    final Change current = new Change(money);
                    p.save();
                    toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ".");
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        PlayerInfo p2 = null;
                        if (name != null && name.length() > 0) {
                            p2 = PlayerInfoFactory.createPlayerInfo(name);
                        }
                        else {
                            p2 = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
                        }
                        if (p2 == null) {
                            toReturn.put("error", "No player found with the name " + name + ".");
                            break Label_0827;
                        }
                        p2.load();
                        if (p2.wurmId <= 0L) {
                            toReturn.put("error", "No player found with the wurmid " + p2.wurmId + ".");
                            break Label_0827;
                        }
                        p2.setMoney(p2.money + moneyToAdd);
                        final Change change2 = new Change(moneyToAdd);
                        final Change current2 = new Change(p2.money);
                        p2.save();
                        toReturn.put("ok", "An amount of " + change2.getChangeString() + " has been added to the account. Current balance is " + current2.getChangeString() + ". It may take a while to reach your server.");
                        if (ingame) {
                            break Label_0827;
                        }
                        if (Servers.localServer.id != p2.currentServer) {
                            new MoneyTransfer(p2.getName(), p2.wurmId, p2.money, moneyToAdd, transactionDetail, (byte)6, "", false);
                            break Label_0827;
                        }
                        new MoneyTransfer(p2.getName(), p2.wurmId, p2.money, moneyToAdd, transactionDetail, (byte)6, "");
                    }
                    catch (IOException iox) {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ": " + wurmId + "," + iox.getMessage(), iox);
                        throw new RemoteException("An error occured. Please contact customer support.");
                    }
                }
                catch (IOException iox2) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + wurmId + "," + iox2.getMessage(), iox2);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
                catch (Exception ex) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + wurmId + "," + ex.getMessage(), ex);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
            }
            // monitorexit(Server.SYNC_LOCK)
            return toReturn;
        }
    }
    
    @Override
    public long chargeMoney(final String playerName, final long moneyToCharge) throws RemoteException {
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " ChargeMoney for player name: " + playerName + ", money: " + moneyToCharge);
        if (Servers.localServer.id == Servers.loginServer.id) {
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                if (p.money <= 0L) {
                    return -10L;
                }
                if (p.money - moneyToCharge < 0L) {
                    return -10L;
                }
                p.setMoney(p.money - moneyToCharge);
                WebInterfaceImpl.logger.info(String.valueOf(playerName) + " was charged " + moneyToCharge + " and now has " + p.money);
                return p.money;
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(playerName) + ": " + iox.getMessage(), iox);
                return -10L;
            }
        }
        WebInterfaceImpl.logger.warning(String.valueOf(playerName) + " cannot charge " + moneyToCharge + " as this server is not the login server");
        return -10L;
    }
    
    @Override
    public Map<String, String> addPlayingTime(final String name, final int months, final int days, final String transactionDetail) throws RemoteException {
        return this.addPlayingTime(name, months, days, transactionDetail, true);
    }
    
    @Override
    public Map<String, String> addPlayingTime(final String name, final int months, final int days, final String transactionDetail, final boolean addSleepPowder) throws RemoteException {
        synchronized (Server.SYNC_LOCK) {
            final Map<String, String> toReturn = new HashMap<String, String>();
            if (name == null || name.length() == 0 || transactionDetail == null || transactionDetail.length() == 0) {
                toReturn.put("error", "Illegal arguments. Check if name or transaction detail is null or empty strings.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            if (months < 0 || days < 0) {
                toReturn.put("error", "Illegal arguments. Make sure that the values for days and months are not negative.");
                // monitorexit(Server.SYNC_LOCK)
                return toReturn;
            }
            final boolean ok = true;
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Addplayingtime for player name: " + name + ", months: " + months + ", days: " + days + ", transactionDetail: " + transactionDetail);
            final SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
            // monitorenter(sync_LOCK = Server.SYNC_LOCK)
            try {
                long timeToAdd = 0L;
                if (days != 0) {
                    timeToAdd = days * 86400000L;
                }
                if (months != 0) {
                    timeToAdd += months * 86400000L * 30L;
                }
                try {
                    final Player p = Players.getInstance().getPlayer(name);
                    long currTime = p.getPaymentExpire();
                    if (timeToAdd > 0L) {
                        if (currTime <= 0L) {
                            Server.addNewPlayer(p.getName());
                        }
                        else {
                            Server.incrementOldPremiums(p.getName());
                        }
                    }
                    currTime = Math.max(currTime, System.currentTimeMillis());
                    currTime += timeToAdd;
                    try {
                        p.setPaymentExpire(currTime);
                        new TimeTransfer(p.getName(), p.getWurmId(), months, addSleepPowder, days, transactionDetail);
                    }
                    catch (IOException iox) {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(p.getName()) + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
                    }
                    final boolean entryServer = Servers.localServer.entryServer;
                    String expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ".";
                    if (entryServer) {
                        expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ". NOTE that it is not effective until you use a portal.";
                    }
                    p.save();
                    toReturn.put("ok", expireString);
                    final Message mess = new Message(null, (byte)3, ":Event", expireString);
                    mess.setReceiver(p.getWurmId());
                    Server.getInstance().addMessage(mess);
                    WebInterfaceImpl.logger.info(String.valueOf(p.getName()) + ' ' + expireString);
                    if (addSleepPowder) {
                        try {
                            final Item inventory = p.getInventory();
                            for (int x = 0; x < months; ++x) {
                                final Item i = ItemFactory.createItem(666, 99.0f, "");
                                inventory.insertItem(i, true);
                            }
                            WebInterfaceImpl.logger.log(Level.INFO, "Inserted " + months + " sleep powder in " + p.getName() + " inventory " + inventory.getWurmId());
                            final Message rmess = new Message(null, (byte)3, ":Event", "You have received " + months + " sleeping powders in your inventory.");
                            rmess.setReceiver(p.getWurmId());
                            Server.getInstance().addMessage(rmess);
                        }
                        catch (Exception ex) {
                            WebInterfaceImpl.logger.log(Level.INFO, ex.getMessage(), ex);
                        }
                    }
                    // monitorexit(sync_LOCK)
                    // monitorexit(Server.SYNC_LOCK)
                    return toReturn;
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        final PlayerInfo p2 = PlayerInfoFactory.createPlayerInfo(name);
                        p2.load();
                        if (p2.wurmId > 0L) {
                            long currTime2 = p2.getPaymentExpire();
                            if (timeToAdd > 0L) {
                                if (currTime2 <= 0L) {
                                    Server.addNewPlayer(p2.getName());
                                }
                                else {
                                    Server.incrementOldPremiums(p2.getName());
                                }
                            }
                            currTime2 = Math.max(currTime2, System.currentTimeMillis());
                            currTime2 += timeToAdd;
                            try {
                                p2.setPaymentExpire(currTime2);
                            }
                            catch (IOException iox2) {
                                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(p2.getName()) + ": failed to set expire to " + currTime2 + ", " + iox2.getMessage(), iox2);
                            }
                            final ServerEntry entry = Servers.getServerWithId(p2.currentServer);
                            String expireString2 = "Your premier playing time has expired now.";
                            if (System.currentTimeMillis() < currTime2) {
                                if (entry.entryServer) {
                                    expireString2 = "You now have premier playing time until " + formatter.format(new Date(currTime2)) + ". Your in game player account will be updated shortly. NOTE that you will have to use a portal to get to the premium servers in order to benefit from it.";
                                }
                                else {
                                    expireString2 = "You now have premier playing time until " + formatter.format(new Date(currTime2)) + ". Your in game player account will be updated shortly.";
                                }
                            }
                            p2.save();
                            toReturn.put("ok", expireString2);
                            WebInterfaceImpl.logger.info(String.valueOf(p2.getName()) + ' ' + expireString2);
                            if (p2.currentServer != Servers.localServer.id) {
                                new TimeTransfer(name, p2.wurmId, months, addSleepPowder, days, transactionDetail, false);
                            }
                            else {
                                new TimeTransfer(p2.getName(), p2.wurmId, months, addSleepPowder, days, transactionDetail);
                                if (addSleepPowder) {
                                    try {
                                        final long inventoryId = DbCreatureStatus.getInventoryIdFor(p2.wurmId);
                                        for (int x2 = 0; x2 < months; ++x2) {
                                            final Item j = ItemFactory.createItem(666, 99.0f, "");
                                            j.setParentId(inventoryId, true);
                                            j.setOwnerId(p2.wurmId);
                                        }
                                        WebInterfaceImpl.logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline " + p2.getName() + " inventory " + inventoryId);
                                    }
                                    catch (Exception ex) {
                                        WebInterfaceImpl.logger.log(Level.INFO, ex.getMessage(), ex);
                                    }
                                }
                            }
                            // monitorexit(sync_LOCK)
                            // monitorexit(Server.SYNC_LOCK)
                            return toReturn;
                        }
                        toReturn.put("error", "No player found with the name " + name + ".");
                        // monitorexit(sync_LOCK)
                        // monitorexit(Server.SYNC_LOCK)
                        return toReturn;
                    }
                    catch (IOException iox3) {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + iox3.getMessage(), iox3);
                        new RemoteException("An error occured. Please contact customer support.");
                    }
                }
                catch (IOException iox4) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + iox4.getMessage(), iox4);
                    new RemoteException("An error occured. Please contact customer support.");
                }
                catch (Exception ex2) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(name) + ":" + ex2.getMessage(), ex2);
                    new RemoteException("An error occured. Please contact customer support.");
                }
            }
            finally {}
        }
		return null;
    }
    
    @Override
    public Map<Integer, String> getDeeds() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getDeeds");
        }
        final Map<Integer, String> toReturn = new HashMap<Integer, String>();
        final Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            toReturn.put(vills[x].id, vills[x].getName());
        }
        return toReturn;
    }
    
    @Override
    public Map<String, ?> getDeedSummary(final int aVillageID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getDeedSummary for villageID: " + aVillageID);
        }
        try {
            final Village village = Villages.getVillage(aVillageID);
            final Map<String, Object> toReturn = new HashMap<String, Object>();
            toReturn.put("Villageid", village.getId());
            toReturn.put("Deedid", village.getDeedId());
            toReturn.put("Name", village.getName());
            toReturn.put("Motto", village.getMotto());
            toReturn.put("Location", Kingdoms.getNameFor(village.kingdom));
            toReturn.put("Size", (village.getEndX() - village.getStartX()) / 2);
            toReturn.put("Founder", village.getFounderName());
            toReturn.put("Mayor", village.mayorName);
            if (village.disband > 0L) {
                toReturn.put("Disbanding in", Server.getTimeFor(village.disband - System.currentTimeMillis()));
                toReturn.put("Disbander", Players.getInstance().getNameFor(village.disbander));
            }
            toReturn.put("Citizens", village.citizens.size());
            toReturn.put("Allies", village.getAllies().length);
            if (village.guards != null) {
                toReturn.put("guards", village.guards.size());
            }
            try {
                final short[] sp = village.getTokenCoords();
                toReturn.put("Token Coord x", (int)sp[0]);
                toReturn.put("Token Coord y", (int)sp[1]);
            }
            catch (NoSuchItemException ex2) {}
            return toReturn;
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }
    
    @Override
    public Map<String, Long> getPlayersForDeed(final int aVillageID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayersForDeed for villageID: " + aVillageID);
        }
        final Map<String, Long> toReturn = new HashMap<String, Long>();
        try {
            final Village village = Villages.getVillage(aVillageID);
            final Citizen[] citizens = village.getCitizens();
            for (int x = 0; x < citizens.length; ++x) {
                if (WurmId.getType(citizens[x].getId()) == 0) {
                    try {
                        toReturn.put(Players.getInstance().getNameFor(citizens[x].getId()), new Long(citizens[x].getId()));
                    }
                    catch (NoSuchPlayerException ex2) {}
                }
            }
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }
    
    @Override
    public Map<String, Integer> getAlliesForDeed(final int aVillageID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getAlliesForDeed for villageID: " + aVillageID);
        }
        final Map<String, Integer> toReturn = new HashMap<String, Integer>();
        try {
            final Village village = Villages.getVillage(aVillageID);
            final Village[] allies = village.getAllies();
            for (int x = 0; x < allies.length; ++x) {
                toReturn.put(allies[x].getName(), allies[x].getId());
            }
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }
    
    @Override
    public String[] getHistoryForDeed(final int villageID, final int maxLength) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getHistoryForDeed for villageID: " + villageID + ", maxLength: " + maxLength);
        }
        try {
            final Village village = Villages.getVillage(villageID);
            return village.getHistoryAsStrings(maxLength);
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }
    
    @Override
    public String[] getAreaHistory(final int maxLength) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getAreaHistory maxLength: " + maxLength);
        }
        return HistoryManager.getHistory(maxLength);
    }
    
    @Override
    public Map<String, ?> getItemSummary(final long aWurmID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getItemSummary for WurmId: " + aWurmID);
        }
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        try {
            final Item item = Items.getItem(aWurmID);
            toReturn.put("WurmId", new Long(aWurmID));
            toReturn.put("Name", item.getName());
            toReturn.put("QL", String.valueOf(item.getQualityLevel()));
            toReturn.put("DMG", String.valueOf(item.getDamage()));
            toReturn.put("SizeX", String.valueOf(item.getSizeX()));
            toReturn.put("SizeY", String.valueOf(item.getSizeY()));
            toReturn.put("SizeZ", String.valueOf(item.getSizeZ()));
            if (item.getOwnerId() != -10L) {
                toReturn.put("Owner", new Long(item.getOwnerId()));
            }
            else {
                toReturn.put("Last owner", new Long(item.lastOwner));
            }
            toReturn.put("Coord x", (int)item.getPosX() >> 2);
            toReturn.put("Coord y", (int)item.getPosY() >> 2);
            toReturn.put("Creator", item.creator);
            toReturn.put("Creationdate", WurmCalendar.getTimeFor(item.creationDate));
            toReturn.put("Description", item.getDescription());
            toReturn.put("Material", Item.getMaterialString(item.getMaterial()));
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> getPlayerIPAddresses() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayerIPAddresses");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        final Player[] playerArr = Players.getInstance().getPlayersByIp();
        for (int x = 0; x < playerArr.length; ++x) {
            if (playerArr[x].getSaveFile().getPower() == 0) {
                toReturn.put(playerArr[x].getName(), playerArr[x].getSaveFile().getIpaddress());
            }
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> getNameBans() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getNameBans");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        final BannedIp[] bips = Players.getInstance().getPlayersBanned();
        if (bips.length > 0) {
            for (int x = 0; x < bips.length; ++x) {
                final long daytime = bips[x].expiry - System.currentTimeMillis();
                toReturn.put(bips[x].ipaddress, String.valueOf(Server.getTimeFor(daytime)) + ", " + bips[x].reason);
            }
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> getIPBans() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getIPBans");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        final BannedIp[] bips = Players.getInstance().getIpsBanned();
        if (bips.length > 0) {
            for (int x = 0; x < bips.length; ++x) {
                final long daytime = bips[x].expiry - System.currentTimeMillis();
                toReturn.put(bips[x].ipaddress, String.valueOf(Server.getTimeFor(daytime)) + ", " + bips[x].reason);
            }
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> getWarnings() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getWarnings");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Not implemented", "Need a name to check.");
        return toReturn;
    }
    
    @Override
    public String getWurmTime() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getWurmTime");
        }
        return WurmCalendar.getTime();
    }
    
    @Override
    public String getUptime() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getUptime");
        }
        return Server.getTimeFor(System.currentTimeMillis() - Server.getStartTime());
    }
    
    @Override
    public String getNews() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getNews");
        }
        return NewsInfo.getInfo();
    }
    
    @Override
    public String getGameInfo() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getGameInfo");
        }
        return String.valueOf(WurmInfo.getInfo()) + WurmInfo2.getInfo();
    }
    
    @Override
    public Map<String, String> getKingdomInfluence() throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getKingdomInfluence");
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        Zones.calculateZones(false);
        final Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        for (int x = 0; x < kingdoms.length; ++x) {
            toReturn.put("Percent controlled by " + kingdoms[x].getName(), WebInterfaceImpl.twoDecimals.format(Zones.getPercentLandForKingdom(kingdoms[x].getId())));
        }
        return toReturn;
    }
    
    @Override
    public Map<String, ?> getMerchantSummary(final long aWurmID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getMerchantSummary for WurmID: " + aWurmID);
        }
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        toReturn.put("Not implemented", "not yet");
        return toReturn;
    }
    
    @Override
    public Map<String, ?> getBankAccount(final long aPlayerID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getBankAccount for playerid: " + aPlayerID);
        }
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        WebInterfaceImpl.logger.log(Level.INFO, "GetBankAccount " + aPlayerID);
        try {
            final Bank lBank = Banks.getBank(aPlayerID);
            if (lBank != null) {
                toReturn.put("BankID", lBank.id);
                toReturn.put("Owner", lBank.owner);
                toReturn.put("StartedMoving", lBank.startedMoving);
                toReturn.put("Open", lBank.open);
                toReturn.put("Size", lBank.size);
                try {
                    final Village lCurrentVillage = lBank.getCurrentVillage();
                    if (lCurrentVillage != null) {
                        toReturn.put("CurrentVillageID", lCurrentVillage.getId());
                        toReturn.put("CurrentVillageName", lCurrentVillage.getName());
                    }
                }
                catch (BankUnavailableException ex) {}
                final int lTargetVillageID = lBank.targetVillage;
                if (lTargetVillageID > 0) {
                    toReturn.put("TargetVillageID", lTargetVillageID);
                }
                final BankSlot[] lSlots = lBank.slots;
                if (lSlots != null && lSlots.length > 0) {
                    final Map<Long, String> lItemsMap = new HashMap<Long, String>(lSlots.length + 1);
                    for (int i = 0; i < lSlots.length; ++i) {
                        if (lSlots[i] == null) {
                            WebInterfaceImpl.logger.log(Level.INFO, "Weird. Bank Slot " + i + " is null for " + aPlayerID);
                        }
                        else {
                            final Item lItem = lSlots[i].item;
                            if (lItem != null) {
                                lItemsMap.put(lItem.getWurmId(), String.valueOf(lItem.getName()) + ", Inserted: " + lSlots[i].inserted + ", Stasis: " + lSlots[i].stasis);
                            }
                        }
                    }
                    if (lItemsMap != null && lItemsMap.size() > 0) {
                        toReturn.put("Items", lItemsMap);
                    }
                }
            }
            else {
                toReturn.put("Error", "Cannot find bank for player ID " + aPlayerID);
            }
        }
        catch (RuntimeException e) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            toReturn.put("Error", "Problem getting bank account for player ID " + aPlayerID + ", " + e);
        }
        return toReturn;
    }
    
    @Override
    public Map<String, ?> authenticateUser(final String playerName, final String emailAddress, final String hashedIngamePassword, final Map params) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " authenticateUser for player name: " + playerName);
        }
        Map<String, Object> toReturn = new HashMap<String, Object>();
        if (Constants.maintaining) {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "The server is currently unavailable.");
            toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
            return toReturn;
        }
        try {
            boolean ver = false;
            Object answer = params.get("VerifiedPayPalAccount");
            if (answer != null && answer instanceof Boolean) {
                ver = (boolean)answer;
            }
            boolean rev = false;
            answer = params.get("ChargebackOrReversal");
            if (answer != null && answer instanceof Boolean) {
                rev = (boolean)answer;
            }
            final Date lastReversal = (Date) params.get("LastChargebackOrReversal");
            final Date first = (Date) params.get("FirstTransactionDate");
            final Date last = (Date) params.get("LastTransactionDate");
            int total = 0;
            answer = params.get("TotalEurosSuccessful");
            if (answer != null && answer instanceof Integer) {
                total = (int)answer;
                if (total < 0) {
                    total = 0;
                }
            }
            int lastMonthEuros = 0;
            answer = params.get("LastMonthEurosSuccessful");
            if (answer != null && answer instanceof Integer) {
                lastMonthEuros = (int)answer;
                if (lastMonthEuros < 0) {
                    lastMonthEuros = 0;
                }
            }
            final String ipAddress = (String) params.get("IP");
            if (ipAddress != null) {
                WebInterfaceImpl.logger.log(Level.INFO, "IP:" + ipAddress);
                final Long lastAttempt = WebInterfaceImpl.ipAttempts.get(ipAddress);
                if (lastAttempt != null && System.currentTimeMillis() - lastAttempt < 5000L) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Too many logon attempts. Please try again in a few seconds.");
                    toReturn.put("display_text0", "Too many logon attempts. Please try again in a few seconds.");
                    return toReturn;
                }
                WebInterfaceImpl.ipAttempts.put(ipAddress, System.currentTimeMillis());
            }
            final PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            if (file.undeadType != 0) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                toReturn.put("display_text0", "Undeads not allowed in here!");
                return toReturn;
            }
            try {
                file.load();
                if (file.undeadType != 0) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                    toReturn.put("display_text0", "Undeads not allowed in here!");
                    return toReturn;
                }
            }
            catch (IOException iox) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "An error occurred when loading your account.");
                toReturn.put("display_text0", "An error occurred when loading your account.");
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
                return toReturn;
            }
            if (!file.overRideShop && rev && (lastReversal == null || last == null || lastReversal.after(last))) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "This paypal account has reversed transactions registered.");
                toReturn.put("display_text0", "This paypal account has reversed transactions registered.");
                return toReturn;
            }
            toReturn = (Map<String, Object>)this.authenticateUser(playerName, emailAddress, hashedIngamePassword);
            final Integer max = (Integer) toReturn.get("maximum_silver0");
            if (max != null) {
                int maxval = max;
                if (file.overRideShop) {
                    maxval = 50 + Math.min(50, (int)(file.playingTime / 3600000L * 3L));
                    toReturn.put("maximum_silver0", maxval);
                }
                else if (lastMonthEuros >= 400) {
                    maxval = 0;
                    toReturn.put("maximum_silver0", maxval);
                    toReturn.put("display_text0", "You may only purchase 400 silver via PayPal per month");
                }
            }
            return toReturn;
        }
        catch (Exception ew) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Error: " + ew.getMessage(), ew);
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "An error occured.");
            return toReturn;
        }
    }
    
    @Override
    public Map<String, String> doesPlayerExist(final String playerName) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " doesPlayerExist for player name: " + playerName);
        }
        final Map<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("ResponseCode", "NOTOK");
            toReturn.put("ErrorMessage", "The server is currently unavailable.");
            toReturn.put("display_text", "The server is currently unavailable.");
            return toReturn;
        }
        toReturn.put("ResponseCode", "OK");
        if (playerName != null) {
            final PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                file.load();
                if (file.wurmId <= 0L) {
                    toReturn.clear();
                    toReturn.put("ResponseCode", "NOTOK");
                    toReturn.put("ErrorMessage", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
                    toReturn.put("display_text", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
                }
            }
            catch (Exception ex) {
                toReturn.clear();
                toReturn.put("ResponseCode", "NOTOK");
                toReturn.put("ErrorMessage", ex.getMessage());
                toReturn.put("display_text", "An error occurred on the " + Servers.localServer.name + " game server: " + ex.getMessage());
            }
        }
        return toReturn;
    }
    
    @Override
    public Map<String, ?> authenticateUser(final String playerName, final String emailAddress, final String hashedIngamePassword) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " authenticateUser for player name: " + playerName);
        }
        final Map<String, Object> toReturn = new HashMap<String, Object>();
        if (Constants.maintaining) {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "The server is currently unavailable.");
            toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
            return toReturn;
        }
        if (playerName != null) {
            final PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            if (file.undeadType != 0) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                toReturn.put("display_text0", "Undeads not allowed in here!");
                return toReturn;
            }
            try {
                file.load();
                if (file.undeadType != 0) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                    toReturn.put("display_text0", "Undeads not allowed in here!");
                    return toReturn;
                }
                if (file.wurmId <= 0L) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "No such player.");
                }
                else if (hashedIngamePassword.equals(file.getPassword())) {
                    if (Servers.isThisLoginServer()) {
                        final LoginServerWebConnection lsw = new LoginServerWebConnection(file.currentServer);
                        final Map<String, String> m = lsw.doesPlayerExist(playerName);
                        final String resp = m.get("ResponseCode");
                        if (resp != null && resp.equals("NOTOK")) {
                            toReturn.put("ResponseCode0", "NOTOK");
                            toReturn.put("ErrorMessage0", m.get("ErrorMessage"));
                            toReturn.put("display_text0", m.get("display_text"));
                            return toReturn;
                        }
                    }
                    toReturn.put("ErrorMessage0", "");
                    if (file.getPaymentExpire() < 0L) {
                        toReturn.put("display_text0", "You are new to the game and may give away an in-game referral to the person who introduced you to Wurm Online using the chat command '/refer' if you purchase premium game time.");
                    }
                    else {
                        toReturn.put("display_text0", "Don't forget to use the in-game '/refer' chat command to refer the one who introduced you to Wurm Online.");
                    }
                    if (file.getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                        toReturn.put("display_text0", "You have less than a week left of premium game time so the amount of coins you can purchase is somewhat limited.");
                        toReturn.put("maximum_silver0", 10);
                    }
                    else {
                        toReturn.put("maximum_silver0", 20 + Math.min(100, (int)(file.playingTime / 3600000L * 3L)));
                    }
                    if (!file.overRideShop && file.isBanned()) {
                        toReturn.put("PurchaseOk0", "NOTOK");
                        toReturn.put("maximum_silver0", 0);
                        toReturn.put("display_text0", "You have been banned. Reason: " + file.banreason);
                        toReturn.put("ErrorMessage0", "The player has been banned. Reason: " + file.banreason);
                    }
                    else {
                        toReturn.put("PurchaseOk0", "OK");
                    }
                    int maxMonths = 0;
                    if (file.getPaymentExpire() > System.currentTimeMillis()) {
                        final long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - file.getPaymentExpire();
                        maxMonths = (int)(maxMonthsMillis / 2419200000L);
                        if (maxMonths < 0) {
                            maxMonths = 0;
                        }
                    }
                    else {
                        maxMonths = 12;
                    }
                    toReturn.put("maximum_months0", maxMonths);
                    toReturn.put("new_customer0", file.getPaymentExpire() <= 0L);
                    toReturn.put("ResponseCode0", "OK");
                    toReturn.put("PlayerID0", new Long(file.wurmId));
                    toReturn.put("ingameBankBalance0", new Long(file.money));
                    toReturn.put("PlayingTimeExpire0", new Long(file.getPaymentExpire()));
                }
                else {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Password does not match.");
                }
            }
            catch (Exception ex) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", ex.getMessage());
                WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        else if (isEmailValid(emailAddress)) {
            final PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
            for (int x = 0; x < infos.length; ++x) {
                if (infos[x].getPassword().equals(hashedIngamePassword)) {
                    toReturn.put("ErrorMessage" + x, "");
                    if (infos[x].getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                        toReturn.put("maximum_silver" + x, 10);
                    }
                    else {
                        toReturn.put("maximum_silver" + x, 10 + Math.min(100, (int)(infos[x].playingTime / 86400000L)));
                    }
                    if (!infos[x].overRideShop && infos[x].isBanned()) {
                        toReturn.put("PurchaseOk" + x, "NOTOK");
                        toReturn.put("maximum_silver" + x, 0);
                        toReturn.put("display_text" + x, "You have been banned. Reason: " + infos[x].banreason);
                        toReturn.put("ErrorMessage" + x, "The player has been banned. Reason: " + infos[x].banreason);
                    }
                    else {
                        toReturn.put("PurchaseOk" + x, "OK");
                    }
                    int maxMonths2 = 0;
                    if (infos[x].getPaymentExpire() > System.currentTimeMillis()) {
                        final long maxMonthsMillis2 = System.currentTimeMillis() + 36288000000L - infos[x].getPaymentExpire();
                        maxMonths2 = (int)(maxMonthsMillis2 / 2419200000L);
                        if (maxMonths2 < 0) {
                            maxMonths2 = 0;
                        }
                    }
                    else {
                        maxMonths2 = 12;
                    }
                    toReturn.put("maximum_months" + x, maxMonths2);
                    toReturn.put("new_customer" + x, infos[x].getPaymentExpire() <= 0L);
                    toReturn.put("ResponseCode" + x, "OK");
                    toReturn.put("PlayerID" + x, new Long(infos[x].wurmId));
                    toReturn.put("ingameBankBalance" + x, new Long(infos[x].money));
                    toReturn.put("PlayingTimeExpire" + x, new Long(infos[x].getPaymentExpire()));
                }
                else {
                    toReturn.put("ResponseCode" + x, "NOTOK");
                    toReturn.put("ErrorMessage" + x, "Password does not match.");
                }
            }
        }
        else {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "Invalid email: " + emailAddress);
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> changePassword(final String playerName, String emailAddress, final String newPassword) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        try {
            toReturn.put("Result", "Unknown email.");
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Changepassword Name: " + playerName + ", email: " + emailAddress);
            if (emailAddress != null) {
                if (isEmailValid(emailAddress)) {
                    final PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                    int nums = 0;
                    for (int x = 0; x < infos.length; ++x) {
                        if (infos[x].getPower() == 0) {
                            try {
                                infos[x].updatePassword(newPassword);
                                if (infos[x].currentServer != Servers.localServer.id) {
                                    new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                                }
                                ++nums;
                                toReturn.put("Account" + nums, String.valueOf(infos[x].getName()) + " password was updated.");
                            }
                            catch (IOException iox) {
                                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to update password for " + infos[x].getName(), iox);
                                toReturn.put("Error" + nums, String.valueOf(infos[x].getName()) + " password was _not_ updated.");
                            }
                        }
                        else {
                            toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
                            WebInterfaceImpl.logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x].getPower());
                        }
                    }
                    if (nums > 0) {
                        toReturn.put("Result", String.valueOf(nums) + " player accounts were affected.");
                    }
                    else {
                        toReturn.put("Error", String.valueOf(nums) + " player accounts were affected.");
                    }
                    return toReturn;
                }
                toReturn.put("Error", String.valueOf(emailAddress) + " is an invalid email.");
            }
            else if (playerName != null) {
                final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
                try {
                    p.load();
                    if (isEmailValid(p.emailAddress)) {
                        emailAddress = p.emailAddress;
                        final PlayerInfo[] infos2 = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                        int nums2 = 0;
                        boolean failed = false;
                        for (int x2 = 0; x2 < infos2.length; ++x2) {
                            if (infos2[x2].getPower() == 0) {
                                try {
                                    infos2[x2].updatePassword(newPassword);
                                    if (infos2[x2].currentServer != Servers.localServer.id) {
                                        new PasswordTransfer(infos2[x2].getName(), infos2[x2].wurmId, infos2[x2].getPassword(), System.currentTimeMillis(), false);
                                    }
                                    ++nums2;
                                    toReturn.put("Account" + nums2, String.valueOf(infos2[x2].getName()) + " password was updated.");
                                }
                                catch (IOException iox3) {
                                    failed = true;
                                    toReturn.put("Error" + nums2, "Failed to update password for a player.");
                                }
                            }
                            else {
                                failed = true;
                                WebInterfaceImpl.logger.warning("Failed to update password for " + infos2[x2].getName() + " as power is " + infos2[x2].getPower());
                            }
                        }
                        if (nums2 > 0) {
                            toReturn.put("Result", String.valueOf(nums2) + " player accounts were affected.");
                        }
                        else {
                            toReturn.put("Error", String.valueOf(nums2) + " player accounts were affected.");
                        }
                        if (failed) {
                            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                        }
                        return toReturn;
                    }
                    toReturn.put("Error", String.valueOf(emailAddress) + " is an invalid email.");
                }
                catch (IOException iox2) {
                    toReturn.put("Error", "Failed to load player data. Password not changed.");
                    WebInterfaceImpl.logger.log(Level.WARNING, iox2.getMessage(), iox2);
                }
            }
            return toReturn;
        }
        finally {
            WebInterfaceImpl.logger.info("Changepassword Name: " + playerName + ", email: " + emailAddress + ", exit: " + toReturn);
        }
    }
    
    @Override
    public Map<String, String> changePassword(final String playerName, String emailAddress, final String hashedOldPassword, final String newPassword) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Result", "Unknown email.");
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Changepassword 2 for player name: " + playerName);
        if (emailAddress != null) {
            if (isEmailValid(emailAddress)) {
                final PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                boolean ok = false;
                int nums = 0;
                for (int x = 0; x < infos.length; ++x) {
                    if (infos[x].getPassword().equals(hashedOldPassword)) {
                        ok = true;
                    }
                }
                if (ok) {
                    boolean failed = false;
                    for (int x2 = 0; x2 < infos.length; ++x2) {
                        if (infos[x2].getPower() == 0) {
                            try {
                                infos[x2].updatePassword(newPassword);
                                if (infos[x2].currentServer != Servers.localServer.id) {
                                    new PasswordTransfer(infos[x2].getName(), infos[x2].wurmId, infos[x2].getPassword(), System.currentTimeMillis(), false);
                                }
                                ++nums;
                                toReturn.put("Account" + nums, String.valueOf(infos[x2].getName()) + " password was updated.");
                            }
                            catch (IOException iox2) {
                                failed = true;
                                toReturn.put("Error" + nums, "Failed to update password for " + infos[x2].getName());
                            }
                        }
                        else {
                            failed = true;
                            toReturn.put("Error" + nums, String.valueOf(infos[x2].getName()) + " password was _not_ updated.");
                        }
                    }
                    if (failed) {
                        WebInterfaceImpl.logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                    }
                }
                if (nums > 0) {
                    toReturn.put("Result", String.valueOf(nums) + " player accounts were affected.");
                }
                else {
                    toReturn.put("Error", String.valueOf(nums) + " player accounts were affected.");
                }
                return toReturn;
            }
            toReturn.put("Result", String.valueOf(emailAddress) + " is an invalid email.");
        }
        else if (playerName != null) {
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                boolean ok = false;
                if (isEmailValid(p.emailAddress)) {
                    emailAddress = p.emailAddress;
                    final PlayerInfo[] infos2 = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                    for (int x = 0; x < infos2.length; ++x) {
                        if (infos2[x].getPassword().equals(hashedOldPassword)) {
                            ok = true;
                        }
                    }
                    int nums2 = 0;
                    if (ok) {
                        boolean failed2 = false;
                        for (int x3 = 0; x3 < infos2.length; ++x3) {
                            if (infos2[x3].getPower() == 0) {
                                try {
                                    infos2[x3].updatePassword(newPassword);
                                    if (infos2[x3].currentServer != Servers.localServer.id) {
                                        new PasswordTransfer(infos2[x3].getName(), infos2[x3].wurmId, infos2[x3].getPassword(), System.currentTimeMillis(), false);
                                    }
                                    ++nums2;
                                    toReturn.put("Account" + nums2, String.valueOf(infos2[x3].getName()) + " password was updated.");
                                }
                                catch (IOException iox3) {
                                    failed2 = true;
                                    toReturn.put("Error" + x3, "Failed to update password for " + infos2[x3].getName());
                                }
                            }
                            else {
                                failed2 = true;
                            }
                        }
                        if (failed2) {
                            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                        }
                    }
                    if (nums2 > 0) {
                        toReturn.put("Result", String.valueOf(nums2) + " player accounts were affected.");
                    }
                    else {
                        toReturn.put("Error", String.valueOf(nums2) + " player accounts were affected.");
                    }
                    return toReturn;
                }
                toReturn.put("Error", String.valueOf(emailAddress) + " is an invalid email.");
            }
            catch (IOException iox) {
                toReturn.put("Error", "Failed to load player data. Password not changed.");
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        return toReturn;
    }
    
    @Override
    public Map<String, String> changeEmail(final String playerName, String oldEmailAddress, final String newEmailAddress) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Result", "Unknown email.");
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Change Email for player name: " + playerName);
        if (Constants.maintaining) {
            toReturn.put("Error", "The server is currently unavailable.");
            toReturn.put("Result", "The server is in maintenance mode. Please try later.");
            return toReturn;
        }
        if (oldEmailAddress != null) {
            if (!isEmailValid(oldEmailAddress)) {
                toReturn.put("Error", "The old email address, " + oldEmailAddress + " is an invalid email.");
            }
            else if (!isEmailValid(newEmailAddress)) {
                toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
            }
            else {
                final PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
                int nums = 0;
                for (int x = 0; x < infos.length; ++x) {
                    if (infos[x].getPower() == 0) {
                        infos[x].setEmailAddress(newEmailAddress);
                        ++nums;
                        toReturn.put("Account" + nums, String.valueOf(infos[x].getName()) + " account was affected.");
                    }
                    else {
                        toReturn.put("Account" + nums, String.valueOf(infos[x].getName()) + " account was _not_ affected.");
                    }
                }
                if (nums > 0) {
                    toReturn.put("Result", String.valueOf(nums) + " player accounts were affected.");
                }
                else {
                    toReturn.put("Error", String.valueOf(nums) + " player accounts were affected.");
                }
            }
            return toReturn;
        }
        if (playerName != null) {
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                if (isEmailValid(newEmailAddress)) {
                    oldEmailAddress = p.emailAddress;
                    final PlayerInfo[] infos2 = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
                    int nums2 = 0;
                    for (int x2 = 0; x2 < infos2.length; ++x2) {
                        if (infos2[x2].getPower() == 0) {
                            infos2[x2].setEmailAddress(newEmailAddress);
                            ++nums2;
                            toReturn.put("Account" + nums2, String.valueOf(infos2[x2].getName()) + " account was affected.");
                        }
                        else {
                            toReturn.put("Account" + nums2, String.valueOf(infos2[x2].getName()) + " account was _not_ affected.");
                        }
                    }
                    if (nums2 > 0) {
                        toReturn.put("Result", String.valueOf(nums2) + " player accounts were affected.");
                    }
                    else {
                        toReturn.put("Error", String.valueOf(nums2) + " player accounts were affected.");
                    }
                    return toReturn;
                }
                toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
            }
            catch (IOException iox) {
                toReturn.put("Error", "Failed to load player data. Email not changed.");
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        return toReturn;
    }
    
    @Override
    public String getChallengePhrase(final String playerName) throws RemoteException {
        if (playerName.contains("@")) {
            final PlayerInfo[] pinfos = PlayerInfoFactory.getPlayerInfosForEmail(playerName);
            if (pinfos.length > 0) {
                return pinfos[0].pwQuestion;
            }
            return "Incorrect email.";
        }
        else {
            if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
                WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getChallengePhrase for player name: " + playerName);
            }
            final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                return p.pwQuestion;
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
                return "Error";
            }
        }
    }
    
    @Override
    public String[] getPlayerNamesForEmail(final String emailAddress) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayerNamesForEmail: " + emailAddress);
        }
        final String[] nameArray = PlayerInfoFactory.getAccountsForEmail(emailAddress);
        return nameArray;
    }
    
    @Override
    public String getEmailAddress(final String playerName) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getEmailAddress for player name: " + playerName);
        }
        final PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
        try {
            p.load();
            return p.emailAddress;
        }
        catch (IOException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
            return "Error";
        }
    }
    
    public static String generateRandomPassword() {
        final Random rand = new Random();
        final int length = rand.nextInt(3) + 6;
        final char[] password = new char[length];
        for (int x = 0; x < length; ++x) {
            final int randDecimalAsciiVal = rand.nextInt("abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".length());
            password[x] = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".charAt(randDecimalAsciiVal);
        }
        return String.valueOf(password);
    }
    
    public static final boolean isEmailValid(final String emailAddress) {
        if (emailAddress == null) {
            return false;
        }
        final Matcher m = WebInterfaceImpl.VALID_EMAIL_PATTERN.matcher(emailAddress);
        return m.matches();
    }
    
    @Override
    public Map<String, String> requestPasswordReset(final String email, final String challengePhraseAnswer) throws RemoteException {
        final Map<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("Error0", "The server is currently in maintenance mode.");
            return toReturn;
        }
        boolean ok = false;
        final String password = generateRandomPassword();
        String playernames = "";
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Password reset for email/name: " + email);
        if (challengePhraseAnswer == null || challengePhraseAnswer.length() < 1) {
            toReturn.put("Error0", "The answer is too short.");
            return toReturn;
        }
        Label_0719: {
            if (!email.contains("@")) {
                final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(email);
                if (pinf.loaded) {
                    break Label_0719;
                }
                try {
                    pinf.load();
                    WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(email) + " " + challengePhraseAnswer + " compares to " + pinf.pwAnswer);
                    if (System.currentTimeMillis() - pinf.lastRequestedPassword > 60000L) {
                        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(email) + " time ok. comparing.");
                        if (pinf.pwAnswer.equalsIgnoreCase(challengePhraseAnswer)) {
                            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(email) + " challenge answer correct.");
                            ok = true;
                            playernames = pinf.getName();
                            pinf.updatePassword(password);
                            if (pinf.currentServer != Servers.localServer.id) {
                                new PasswordTransfer(pinf.getName(), pinf.wurmId, pinf.getPassword(), System.currentTimeMillis(), false);
                            }
                        }
                        pinf.lastRequestedPassword = System.currentTimeMillis();
                        break Label_0719;
                    }
                    toReturn.put("Error", "Please try again in a minute.");
                    return toReturn;
                }
                catch (IOException iox) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(email) + ":" + iox.getMessage(), iox);
                    toReturn.put("Error", "An error occured. Please try later.");
                    return toReturn;
                }
            }
            final PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(email);
            for (int x = 0; x < p.length; ++x) {
                try {
                    p[x].load();
                    if (p[x].pwAnswer.toLowerCase().equals(challengePhraseAnswer.toLowerCase()) || (p[x].pwAnswer.length() == 0 && p[x].pwQuestion.length() == 0)) {
                        if (System.currentTimeMillis() - p[x].lastRequestedPassword > 60000L) {
                            ok = true;
                            if (playernames.length() > 0) {
                                playernames = String.valueOf(playernames) + ", " + p[x].getName();
                            }
                            else {
                                playernames = p[x].getName();
                            }
                            p[x].updatePassword(password);
                            if (p[x].currentServer != Servers.localServer.id) {
                                new PasswordTransfer(p[x].getName(), p[x].wurmId, p[x].getPassword(), System.currentTimeMillis(), false);
                            }
                        }
                        else if (!ok) {
                            toReturn.put("Error", "Please try again in a minute.");
                            return toReturn;
                        }
                    }
                    p[x].lastRequestedPassword = System.currentTimeMillis();
                }
                catch (IOException iox2) {
                    WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(email) + ":" + iox2.getMessage(), iox2);
                    toReturn.put("Error", "An error occured. Please try later.");
                    return toReturn;
                }
            }
        }
        if (ok) {
            toReturn.put("Result", "Password was changed.");
        }
        else {
            toReturn.put("Error", "Password was not changed.");
        }
        if (playernames.length() > 0) {
            try {
                String mail = Mailer.getPasswordMail();
                mail = mail.replace("@pname", playernames);
                mail = mail.replace("@password", password);
                Mailer.sendMail(WebInterfaceImpl.mailAccount, email, "Wurm Online password request", mail);
                toReturn.put("MailResult", "A mail was sent to the mail adress: " + email + " for " + playernames + ".");
            }
            catch (Exception ex) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(email) + ":" + ex.getMessage(), ex);
                toReturn.put("MailError", "An error occured - " + ex.getMessage() + ". Please try later.");
            }
            return toReturn;
        }
        toReturn.put("Error", "Wrong answer.");
        return toReturn;
    }
    
    @Override
    public Map<Integer, String> getAllServers() throws RemoteException {
        return this.getAllServerInternalAddresses();
    }
    
    @Override
    public Map<Integer, String> getAllServerInternalAddresses() throws RemoteException {
        final Map<Integer, String> toReturn = new HashMap<Integer, String>();
        final ServerEntry[] entries = Servers.getAllServers();
        for (int x = 0; x < entries.length; ++x) {
            toReturn.put(entries[x].id, entries[x].INTRASERVERADDRESS);
        }
        return toReturn;
    }
    
    @Override
    public boolean sendMail(final String sender, final String receiver, final String subject, final String text) throws RemoteException {
        if (!isEmailValid(sender)) {
            return false;
        }
        if (!isEmailValid(receiver)) {
            return false;
        }
        try {
            Mailer.sendMail(sender, receiver, subject, text);
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    @Override
    public void shutDown(final String playerName, final String password, final String reason, final int seconds) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINE)) {
            WebInterfaceImpl.logger.fine(String.valueOf(this.getRemoteClientDetails()) + " shutDown by player name: " + playerName);
        }
        final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(playerName));
        try {
            pinf.load();
            if (pinf.getPower() >= 4) {
                try {
                    final String pw = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.getName())));
                    if (pw.equals(pinf.getPassword())) {
                        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " player: " + playerName + " initiated shutdown in " + seconds + " seconds: " + reason);
                        if (seconds <= 0) {
                            Server.getInstance().shutDown();
                        }
                        else {
                            Server.getInstance().startShutdown(seconds, reason);
                        }
                    }
                    else {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(this.getRemoteClientDetails()) + " player: " + playerName + " denied shutdown due to wrong password.");
                    }
                }
                catch (Exception ex) {
                    WebInterfaceImpl.logger.log(Level.INFO, "Failed to encrypt password for player " + playerName, ex);
                }
            }
            else {
                WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " player: " + playerName + " DENIED shutdown in " + seconds + " seconds: " + reason);
            }
        }
        catch (IOException iox) {
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " player: " + playerName + ": " + iox.getMessage(), iox);
        }
    }
    
    @Override
    public Map<String, Byte> getReferrers(final long wurmid) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getReferrers for WurmID: " + wurmid);
        }
        return PlayerInfoFactory.getReferrers(wurmid);
    }
    
    @Override
    public String addReferrer(final String receiver, final long referrer) {
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " addReferrer for Receiver player name: " + receiver + ", referrerID: " + referrer);
        synchronized (Server.SYNC_LOCK) {
            try {
                final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(receiver);
                try {
                    pinf.load();
                }
                catch (IOException iox) {
                    // monitorexit(Server.SYNC_LOCK)
                    return String.valueOf(receiver) + " - no such player exists. Please check the spelling.";
                }
                if (pinf.wurmId == referrer) {
                    // monitorexit(Server.SYNC_LOCK)
                    return "You may not refer yourself.";
                }
                if (pinf.getPaymentExpire() <= 0L) {
                    // monitorexit(Server.SYNC_LOCK)
                    return String.valueOf(pinf.getName()) + " has never had a premium account and may not receive referrals.";
                }
                if (PlayerInfoFactory.addReferrer(pinf.wurmId, referrer)) {
                    // monitorexit(Server.SYNC_LOCK)
                    return String.valueOf(pinf.wurmId);
                }
                // monitorexit(Server.SYNC_LOCK)
                return "You have already awarded referral to that player.";
            }
            catch (Exception e) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(e.getMessage()) + " " + receiver + " from " + referrer, e);
                // monitorexit(Server.SYNC_LOCK)
                return "An error occurred. Please write a bug report about this.";
            }
        }
    }
    
    @Override
    public String acceptReferrer(final long wurmid, final String awarderName, final boolean money) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINE)) {
            WebInterfaceImpl.logger.fine(String.valueOf(this.getRemoteClientDetails()) + " acceptReferrer for player wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money);
        }
        PlayerInfo pinf = null;
        try {
            final long l = Long.parseLong(awarderName);
            pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(l);
        }
        catch (NumberFormatException nfe) {
            pinf = PlayerInfoFactory.createPlayerInfo(awarderName);
            try {
                pinf.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
                return "Failed to locate the player " + awarderName + " in the database.";
            }
        }
        if (pinf != null) {
            try {
                synchronized (Server.SYNC_LOCK) {
                    Label_0276: {
                        if (PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
                            Label_0303: {
                                try {
                                    if (money) {
                                        PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Referred by " + pinf.getName());
                                        break Label_0303;
                                    }
                                    PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Referred by " + pinf.getName());
                                    break Label_0303;
                                }
                                catch (Exception ex) {
                                    WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
                                    PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
                                    // monitorexit(Server.SYNC_LOCK)
                                    return "An error occured. Please try later or post a bug report.";
                                }
                            }
                            // monitorexit(Server.SYNC_LOCK)
                            return "Okay, accepted the referral from " + awarderName + ". The reward will arrive soon if it has not already.";
                        }
                    }
                    // monitorexit(Server.SYNC_LOCK)
                    return "Failed to match " + awarderName + " to any existing referral.";
                }
            }
            catch (Exception ex2) {
                WebInterfaceImpl.logger.log(Level.WARNING, ex2.getMessage(), ex2);
                return "An error occured. Please try later or post a bug report.";
            }
        }
        return "Failed to locate " + awarderName + " in the database.";
    }
    
    @Override
    public Map<String, Double> getSkillStats(final int skillid) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getSkillStats for skillid: " + skillid);
        }
        final Map<String, Double> toReturn = new HashMap<String, Double>();
        try {
            final SkillStat sk = SkillStat.getSkillStatForSkill(skillid);
            for (final Map.Entry<Long, Double> entry : sk.stats.entrySet()) {
                final Long lid = entry.getKey();
                final long pid = lid;
                final PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
                if (p != null && entry.getValue() > 1.0) {
                    toReturn.put(p.getName(), entry.getValue());
                }
            }
        }
        catch (Exception ex) {
            WebInterfaceImpl.logger.log(Level.WARNING, ex.getMessage(), ex);
            toReturn.put("ERROR: " + ex.getMessage(), 0.0);
        }
        return toReturn;
    }
    
    @Override
    public Map<Integer, String> getSkills() throws RemoteException {
        return SkillSystem.skillNames;
    }
    
    @Override
    public Map<String, ?> getStructureSummary(final long aStructureID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getStructureSummary for StructureID: " + aStructureID);
        }
        final Map<String, Object> lToReturn = new HashMap<String, Object>(10);
        try {
            final Structure lStructure = Structures.getStructure(aStructureID);
            if (lStructure != null) {
                lToReturn.put("CenterX", lStructure.getCenterX());
                lToReturn.put("CenterY", lStructure.getCenterY());
                lToReturn.put("CreationDate", lStructure.getCreationDate());
                lToReturn.put("Door Count", lStructure.getDoors());
                lToReturn.put("FinalFinished", lStructure.isFinalFinished());
                lToReturn.put("Finalized", lStructure.isFinalized());
                lToReturn.put("Finished", lStructure.isFinished());
                lToReturn.put("Guest Count", lStructure.getPermissionsPlayerList().size());
                lToReturn.put("Limit", lStructure.getLimit());
                lToReturn.put("Lockable", lStructure.isLockable());
                lToReturn.put("Locked", lStructure.isLocked());
                lToReturn.put("MaxX", lStructure.getMaxX());
                lToReturn.put("MaxY", lStructure.getMaxY());
                lToReturn.put("MinX", lStructure.getMinX());
                lToReturn.put("MinY", lStructure.getMinY());
                lToReturn.put("Name", lStructure.getName());
                lToReturn.put("OwnerID", lStructure.getOwnerId());
                lToReturn.put("Roof", lStructure.getRoof());
                lToReturn.put("Size", lStructure.getSize());
                lToReturn.put("HasWalls", lStructure.hasWalls());
                final Wall[] lWalls = lStructure.getWalls();
                if (lWalls != null) {
                    lToReturn.put("Wall Count", lWalls.length);
                }
                else {
                    lToReturn.put("Wall Count", 0);
                }
                lToReturn.put("WritID", lStructure.getWritId());
                lToReturn.put("WurmID", lStructure.getWurmId());
            }
            else {
                lToReturn.put("Error", "No such Structure");
            }
        }
        catch (NoSuchStructureException nss) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Structure with id " + aStructureID + " not found.", (Throwable)nss);
            lToReturn.put("Error", "No such Structure");
            lToReturn.put("Exception", nss.getMessage());
        }
        catch (RuntimeException e) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            lToReturn.put("Exception", e);
        }
        return lToReturn;
    }
    
    @Override
    public long getStructureIdFromWrit(final long aWritID) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getStructureIdFromWrit for WritID: " + aWritID);
        }
        try {
            final Structure struct = Structures.getStructureForWrit(aWritID);
            if (struct != null) {
                return struct.getWurmId();
            }
        }
        catch (NoSuchStructureException ex) {}
        return -1L;
    }
    
    @Override
    public Map<String, ?> getTileSummary(final int tilex, final int tiley, final boolean surfaced) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getTileSummary for tile (x,y): " + tilex + ", " + tiley);
        }
        final Map<String, Object> lToReturn = new HashMap<String, Object>(10);
        try {
            final Zone zone = Zones.getZone(tilex, tiley, surfaced);
            final VolaTile tile = zone.getTileOrNull(tilex, tiley);
            if (tile != null) {
                final Structure lStructure = tile.getStructure();
                if (lStructure != null) {
                    lToReturn.put("StructureID", lStructure.getWurmId());
                    lToReturn.put("StructureName", lStructure.getName());
                }
                lToReturn.put("Kingdom", tile.getKingdom());
                final Village lVillage = tile.getVillage();
                if (lVillage != null) {
                    lToReturn.put("VillageID", lVillage.getId());
                    lToReturn.put("VillageName", lVillage.getName());
                }
                lToReturn.put("Coord x", tile.getTileX());
                lToReturn.put("Coord y", tile.getTileY());
            }
            else {
                lToReturn.put("Error", "No such tile");
            }
        }
        catch (NoSuchZoneException e) {
            lToReturn.put("Error", "No such zone");
            lToReturn.put("Exception", e.getMessage());
        }
        catch (RuntimeException e2) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Error: " + e2.getMessage(), e2);
            lToReturn.put("Exception", e2);
        }
        return lToReturn;
    }
    
    @Override
    public String getReimbursementInfo(final String email) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getReimbursementInfo for email: " + email);
        }
        return Reimbursement.getReimbursementInfo(email);
    }
    
    @Override
    public boolean withDraw(final String retriever, final String name, final String _email, final int _months, final int _silvers, final boolean titlebok, final int _daysLeft) {
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " withDraw for retriever: " + retriever + ", name: " + name + ", email: " + _email + ", months: " + _months + ", silvers: " + _silvers);
        return Reimbursement.withDraw(retriever, name, _email, _months, _silvers, titlebok, _daysLeft);
    }
    
    @Override
    public boolean transferPlayer(final String playerName, final int posx, final int posy, final boolean surfaced, final int power, final byte[] data) {
        if (Constants.maintaining && power <= 0) {
            return false;
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Transferplayer name: " + playerName + ", position (x,y): " + posx + ", " + posy + ", surfaced: " + surfaced);
        return IntraServerConnection.savePlayerToDisk(data, posx, posy, surfaced, false) > 0L && (Servers.isThisLoginServer() || new LoginServerWebConnection().setCurrentServer(playerName, Servers.localServer.id));
    }
    
    @Override
    public boolean changePassword(final long wurmId, final String newPassword) {
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Changepassword name: " + wurmId);
        return IntraServerConnection.setNewPassword(wurmId, newPassword);
    }
    
    @Override
    public boolean setCurrentServer(final String name, final int currentServer) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " setCurrentServer to " + currentServer + " for player name: " + name);
        }
        final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if (pinf == null) {
            return false;
        }
        pinf.setCurrentServer(currentServer);
        return true;
    }
    
    @Override
    public boolean addDraggedItem(final long itemId, final byte[] itemdata, final long draggerId, final int posx, final int posy) {
        final DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " Adddraggeditem itemID: " + itemId + ", draggerId: " + draggerId);
        try {
            final Set<ItemMetaData> idset = new HashSet<ItemMetaData>();
            for (int nums = iis.readInt(), x = 0; x < nums; ++x) {
                IntraServerConnection.createItem(iis, 0.0f, 0.0f, 0.0f, idset, false);
            }
            Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
        }
        catch (IOException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
            return false;
        }
        try {
            final Item i = Items.getItem(itemId);
            final Zone z = Zones.getZone(posx, posy, true);
            z.addItem(i);
            return true;
        }
        catch (NoSuchItemException nsi) {
            WebInterfaceImpl.logger.log(Level.WARNING, nsi.getMessage(), (Throwable)nsi);
            return false;
        }
        catch (NoSuchZoneException nsz) {
            WebInterfaceImpl.logger.log(Level.WARNING, nsz.getMessage(), (Throwable)nsz);
            return false;
        }
    }
    
    @Override
    public String rename(final String oldName, String newName, final String newPass, final int power) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " rename oldName: " + oldName + ", newName: " + newName + ", power: " + power);
        }
        String toReturn = "";
        newName = LoginHandler.raiseFirstLetter(newName);
        if (Servers.localServer.LOGINSERVER && Players.getInstance().doesPlayerNameExist(newName)) {
            return "The name " + newName + " already exists. This is an Error.";
        }
        if (Servers.localServer.LOGINSERVER) {
            toReturn = String.valueOf(toReturn) + Servers.rename(oldName, newName, newPass, power);
        }
        if (!toReturn.contains("Error.")) {
            try {
                toReturn = PlayerInfoFactory.rename(oldName, newName, newPass, power);
            }
            catch (IOException iox) {
                toReturn = String.valueOf(toReturn) + Servers.localServer.name + " " + iox.getMessage() + ". This is an Error.\n";
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        return toReturn;
    }
    
    @Override
    public String changePassword(String changerName, String name, final String newPass, final int power) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " changePassword, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
        }
        String toReturn = "";
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        try {
            toReturn = PlayerInfoFactory.changePassword(changerName, name, newPass, power);
        }
        catch (IOException iox) {
            toReturn = String.valueOf(toReturn) + Servers.localServer.name + " " + iox.getMessage() + "\n";
            WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " changePassword, changerName: " + changerName + ", for player name: " + name);
        if (Servers.localServer.LOGINSERVER) {
            if (changerName.equals(name)) {
                final PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                if (pinf != null && Servers.localServer.id != pinf.currentServer) {
                    final LoginServerWebConnection lsw = new LoginServerWebConnection(pinf.currentServer);
                    toReturn = String.valueOf(toReturn) + lsw.changePassword(changerName, name, newPass, power);
                }
            }
            else {
                toReturn = String.valueOf(toReturn) + Servers.sendChangePass(changerName, name, newPass, power);
            }
        }
        return toReturn;
    }
    
    @Override
    public String changeEmail(String changerName, String name, final String newEmail, final String password, final int power, final String pwQuestion, final String pwAnswer) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " changeEmail, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
        }
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        String toReturn = "";
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " changeEmail, changerName: " + changerName + ", for player name: " + name);
        try {
            toReturn = PlayerInfoFactory.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
            if (toReturn.equals("NO") || toReturn.equals("NO Retrieval info updated.")) {
                return "You may only have 5 accounts with the same email. Also you need to provide the correct password for a character with that email address in order to change to it.";
            }
        }
        catch (IOException iox) {
            toReturn = String.valueOf(toReturn) + Servers.localServer.name + " " + iox.getMessage() + "\n";
            WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        if (Servers.localServer.LOGINSERVER) {
            toReturn = String.valueOf(toReturn) + Servers.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
        }
        return toReturn;
    }
    
    @Override
    public String addReimb(String changerName, String name, final int numMonths, final int _silver, final int _daysLeft, final boolean setbok) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINE)) {
            WebInterfaceImpl.logger.fine(String.valueOf(this.getRemoteClientDetails()) + " addReimb, changerName: " + changerName + ", for player name: " + name + ", numMonths: " + numMonths + ", silver: " + _silver + ", daysLeft: " + _daysLeft + ", setbok: " + setbok);
        }
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        if (Servers.localServer.LOGINSERVER) {
            return Reimbursement.addReimb(changerName, name, numMonths, _silver, _daysLeft, setbok);
        }
        return String.valueOf(Servers.localServer.name) + " - failed to add reimbursement. This is not the login server.";
    }
    
    @Override
    public long[] getCurrentServerAndWurmid(String name, final long wurmid) {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getCurrentServerAndWurmid for player name: " + name + ", wurmid: " + wurmid);
        }
        PlayerInfo pinf = null;
        if (name != null && name.length() > 2) {
            name = LoginHandler.raiseFirstLetter(name);
            pinf = PlayerInfoFactory.createPlayerInfo(name);
        }
        else if (wurmid > 0L) {
            pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        }
        if (pinf != null) {
            try {
                pinf.load();
                final long[] toReturn = { pinf.currentServer, pinf.wurmId };
                return toReturn;
            }
            catch (IOException ex) {}
        }
        return WebInterfaceImpl.noInfoLong;
    }
    
    @Override
    public Map<Long, byte[]> getPlayerStates(final long[] wurmids) throws RemoteException, WurmServerException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            if (wurmids.length == 0) {
                WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayersSubInfo for ALL players.");
            }
            else {
                final StringBuilder buf = new StringBuilder();
                for (int x = 0; x < wurmids.length; ++x) {
                    if (x > 0) {
                        buf.append(",");
                    }
                    buf.append(wurmids[x]);
                }
                WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " getPlayersSubInfo for player wurmids: " + buf.toString());
            }
        }
        return PlayerInfoFactory.getPlayerStates(wurmids);
    }
    
    @Override
    public void manageFeature(final int serverId, final int featureId, final boolean aOverridden, final boolean aEnabled, final boolean global) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " manageFeature " + featureId);
        }
        final Thread t = new Thread("manageFeature-Thread-" + featureId) {
            @Override
            public void run() {
                Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, aOverridden, aEnabled, global);
            }
        };
        t.setPriority(4);
        t.start();
    }
    
    @Override
    public void startShutdown(final String instigator, final int seconds, final String reason) throws RemoteException {
        if (Servers.isThisLoginServer()) {
            Servers.startShutdown(instigator, seconds, reason);
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(instigator) + " shutting down server in " + seconds + " seconds, reason: " + reason);
        Server.getInstance().startShutdown(seconds, reason);
    }
    
    @Override
    public String sendMail(final byte[] maildata, final byte[] itemdata, final long sender, final long wurmid, final int targetServer) {
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " sendMail " + sender + " to server " + targetServer + ", receiver ID: " + wurmid);
        if (targetServer == Servers.localServer.id) {
            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(maildata));
            try {
                for (int nums = dis.readInt(), x = 0; x < nums; ++x) {
                    final WurmMail m = new WurmMail(dis.readByte(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readInt(), dis.readBoolean(), false);
                    WurmMail.addWurmMail(m);
                    m.createInDatabase();
                }
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, iox.getMessage(), iox);
                return "A database error occurred. Please report this to a GM.";
            }
            final DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
            try {
                final Set<ItemMetaData> idset = new HashSet<ItemMetaData>();
                for (int nums2 = iis.readInt(), x2 = 0; x2 < nums2; ++x2) {
                    IntraServerConnection.createItem(iis, 0.0f, 0.0f, 0.0f, idset, false);
                }
                Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
                return "";
            }
            catch (IOException iox2) {
                WebInterfaceImpl.logger.log(Level.WARNING, iox2.getMessage(), iox2);
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
        }
        final ServerEntry entry = Servers.getServerWithId(targetServer);
        if (entry == null) {
            return "Failed to locate target server.";
        }
        if (entry.isAvailable(5, true)) {
            final LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
            return lsw.sendMail(maildata, itemdata, sender, wurmid, targetServer);
        }
        return "The target server is not available right now.";
    }
    
    @Override
    public String pardonban(final String name) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " pardonban for player name: " + name);
        }
        if (!Servers.localServer.LOGINSERVER) {
            WebInterfaceImpl.logger.warning(String.valueOf(Servers.localServer.name) + " not login server. Pardon failed.");
            return String.valueOf(Servers.localServer.name) + " not login server. Pardon failed.";
        }
        final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
        if (info != null) {
            try {
                info.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(this.getRemoteClientDetails()) + " Failed to load the player information. Not pardoned - " + iox.getMessage(), iox);
                return "Failed to load the player information. Not pardoned.";
            }
            try {
                info.setBanned(false, "", 0L);
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(this.getRemoteClientDetails()) + " Failed to save the player information. Not pardoned - " + iox.getMessage(), iox);
                return "Failed to save the player information. Not pardoned.";
            }
            WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Login server pardoned " + name);
            return "Login server pardoned " + name + ".";
        }
        WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
        return "Failed to locate the player " + name + ".";
    }
    
    @Override
    public String ban(final String name, final String reason, final int days) throws RemoteException {
        if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
            WebInterfaceImpl.logger.finer(String.valueOf(this.getRemoteClientDetails()) + " ban for player name: " + name + ", reason: " + reason + ", for " + days + " days");
        }
        if (!Servers.localServer.LOGINSERVER) {
            WebInterfaceImpl.logger.warning(String.valueOf(Servers.localServer.name) + " not login server. Ban failed.");
            return String.valueOf(Servers.localServer.name) + " not login server. Ban failed.";
        }
        final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
        if (info != null) {
            final long expiry = System.currentTimeMillis() + days * 86400000L;
            try {
                info.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load the player information. Not banned - " + iox.getMessage(), iox);
                return "Failed to load the player information. Not banned.";
            }
            try {
                info.setBanned(true, reason, expiry);
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Not banned - " + iox.getMessage(), iox);
                return "Failed to save the player information. Not banned.";
            }
            WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Login server banned " + name + ": " + reason + " for " + days + " days.");
            return "Login server banned " + name + ": " + reason + " for " + days + " days.";
        }
        WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
        return "Failed to locate the player " + name + ".";
    }
    
    @Override
    public String addBannedIp(final String ip, final String reason, final int days) throws RemoteException {
        final long expiry = System.currentTimeMillis() + days * 86400000L;
        Players.getInstance().addBannedIp(ip, reason, expiry);
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " RMI client requested " + ip + " banned for " + days + " days - " + reason);
        return String.valueOf(ip) + " banned for " + days + " days - " + reason;
    }
    
    @Override
    public BannedIp[] getPlayersBanned() {
        return Players.getInstance().getPlayersBanned();
    }
    
    @Override
    public BannedIp[] getIpsBanned() {
        return Players.getInstance().getIpsBanned();
    }
    
    @Override
    public String removeBannedIp(final String ip) {
        if (Players.getInstance().removeBannedIp(ip)) {
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " RMI client requested " + ip + " was pardoned.");
            return "Okay, " + ip + " was pardoned.";
        }
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " RMI client requested pardon but the ip " + ip + " was not previously banned.");
        return "The ip " + ip + " was not previously banned.";
    }
    
    @Override
    public String setPlayerMoney(final long wurmid, final long currentMoney, final long moneyAdded, final String detail) {
        if (WebInterfaceImpl.moneyDetails.contains(detail)) {
            WebInterfaceImpl.logger.warning(String.valueOf(this.getRemoteClientDetails()) + " RMI client The money transaction has already been performed, wurmid: " + wurmid + ", currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
            return "The money transaction has already been performed";
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " RMI client set player money for " + wurmid);
        final PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        Label_0196: {
            if (info != null) {
                Label_0250: {
                    try {
                        info.load();
                        break Label_0250;
                    }
                    catch (IOException iox) {
                        WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ", detail: " + detail + ": " + iox.getMessage(), iox);
                        return "Failed to load the player from database. Transaction failed.";
                    }
                }
                if (info.wurmId > 0L) {
                    if (info.currentServer != Servers.localServer.id) {
                        WebInterfaceImpl.logger.warning("Received a CMD_SET_PLAYER_MONEY for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
                        return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
                    }
                    try {
                        info.setMoney(currentMoney);
                        new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, (byte)6, "");
                        final Change c = new Change(currentMoney);
                        WebInterfaceImpl.moneyDetails.add(detail);
                        try {
                            WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " RMI client Added " + moneyAdded + " to player ID: " + wurmid + ", currentMoney: " + currentMoney + ", detail: " + detail);
                            final Player p = Players.getInstance().getPlayer(wurmid);
                            final Message mess = new Message(null, (byte)3, ":Event", "Your available money in the bank is now " + c.getChangeString() + ".");
                            mess.setReceiver(p.getWurmId());
                            Server.getInstance().addMessage(mess);
                        }
                        catch (NoSuchPlayerException exp) {
                            if (WebInterfaceImpl.logger.isLoggable(Level.FINER)) {
                                WebInterfaceImpl.logger.finer("player ID: " + wurmid + " is not online, currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
                            }
                        }
                        return "Okay. The player now has " + c.getChangeString() + " in the bank.";
                    }
                    catch (IOException iox) {
                        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(wurmid) + ", failed to set money to " + currentMoney + ", detail: " + detail + ".", iox);
                        return "Money transaction failed. Error reported was " + iox.getMessage() + ".";
                    }
                }
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(wurmid) + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
                return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
            }
        }
        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(wurmid) + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
        return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
    }
    
    @Override
    public String setPlayerPremiumTime(final long wurmid, final long currentExpire, final int days, final int months, final String detail) {
        if (WebInterfaceImpl.timeDetails.contains(detail)) {
            WebInterfaceImpl.logger.warning(String.valueOf(this.getRemoteClientDetails()) + " RMI client The time transaction has already been performed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail);
            return "The time transaction has already been performed";
        }
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " RMI client set premium time for " + wurmid);
        final PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if (info != null) {
            try {
                info.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load the player from database. Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail, iox);
                return "Failed to load the player from database. Transaction failed.";
            }
            if (info.currentServer != Servers.localServer.id) {
                WebInterfaceImpl.logger.warning("Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
                return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
            }
            try {
                info.setPaymentExpire(currentExpire);
                new TimeTransfer(info.getName(), wurmid, months, false, days, detail);
                WebInterfaceImpl.timeDetails.add(detail);
                try {
                    final Player p = Players.getInstance().getPlayer(wurmid);
                    final String expireString = "You now have premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
                    final Message mess = new Message(null, (byte)3, ":Event", expireString);
                    mess.setReceiver(p.getWurmId());
                    Server.getInstance().addMessage(mess);
                }
                catch (NoSuchPlayerException ex) {}
                WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " RMI client " + info.getName() + " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ", wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + '.');
                return "Okay. " + info.getName() + " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + ", " + iox.getMessage(), iox);
                return "Time transaction failed. Error reported was " + iox.getMessage() + ".";
            }
        }
        WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(wurmid) + ", failed to locate player info and set expire time to " + currentExpire + "!, detail: " + detail);
        return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
    }
    
    @Override
    public void setWeather(final float windRotation, final float windpower, final float windDir) {
        Server.getWeather().setWindOnly(windRotation, windpower, windDir);
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " RMI client. Received weather data from login server. Propagating windrot=" + windRotation);
        Players.getInstance().setShouldSendWeather(true);
    }
    
    @Override
    public String sendVehicle(final byte[] passengerdata, final byte[] itemdata, final long pilotId, final long vehicleId, final int targetServer, final int tilex, final int tiley, final int layer, final float rot) {
        WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(this.getRemoteClientDetails()) + " RMI client send vehicle for pilot " + pilotId + " vehicle " + vehicleId + " itemdata bytes=" + itemdata.length + " passenger data bytes=" + passengerdata.length);
        if (targetServer == Servers.localServer.id) {
            final long start = System.nanoTime();
            final DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
            final Set<ItemMetaData> idset = new HashSet<ItemMetaData>();
            try {
                final int settingBits = iis.readInt();
                VehicleSettings.setSettings(vehicleId, settingBits, true);
                VehicleSettings.setBit(vehicleId, 5, true, true);
                final int nums = iis.readInt();
                WebInterfaceImpl.logger.log(Level.INFO, "Trying to create " + nums + " items for vehicle: " + vehicleId);
                final float posx = tilex * 4 + 2;
                final float posy = tiley * 4 + 2;
                IntraServerConnection.resetTransferVariables(String.valueOf(pilotId));
                for (int x = 0; x < nums; ++x) {
                    IntraServerConnection.createItem(iis, posx, posy, 0.0f, idset, false);
                }
                Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(iox.getMessage()) + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, iox);
                for (final ItemMetaData md : idset) {
                    WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(md.itname) + ", " + md.itemId);
                }
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
            catch (Exception ex) {
                WebInterfaceImpl.logger.log(Level.WARNING, String.valueOf(ex.getMessage()) + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, ex);
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(passengerdata));
            try {
                final Item i = Items.getItem(vehicleId);
                i.setPosXYZ(tilex * 4 + 2, tiley * 4 + 2, 0.0f);
                i.setRotation(rot);
                WebInterfaceImpl.logger.log(Level.INFO, "Trying to put " + i.getName() + ", " + i.getDescription() + " at " + i.getTileX() + "," + i.getTileY());
                Zones.getZone(i.getTileX(), i.getTileY(), layer == 0).addItem(i);
                Vehicles.createVehicle(i);
                final MountTransfer mt = new MountTransfer(vehicleId, pilotId);
                for (int nums2 = dis.readInt(), x = 0; x < nums2; ++x) {
                    mt.addToSeat(dis.readLong(), dis.readInt());
                }
            }
            catch (NoSuchItemException nsi) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsi.getMessage(), (Throwable)nsi);
            }
            catch (NoSuchZoneException nsz) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsz.getMessage(), (Throwable)nsz);
            }
            catch (IOException iox2) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + iox2.getMessage(), iox2);
                return "A database error occurred. Please report this to a GM.";
            }
            final float lElapsedTime = (System.nanoTime() - start) / 1000000.0f;
            WebInterfaceImpl.logger.log(Level.INFO, "Transferring vehicle " + vehicleId + " took " + lElapsedTime + " ms.");
            return "";
        }
        final ServerEntry entry = Servers.getServerWithId(targetServer);
        if (entry == null) {
            return "Failed to locate target server.";
        }
        if (entry.isAvailable(5, true)) {
            final LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
            return lsw.sendVehicle(passengerdata, itemdata, pilotId, vehicleId, targetServer, tilex, tiley, layer, rot);
        }
        return "The target server is not available right now.";
    }
    
    @Override
    public void genericWebCommand(final short wctype, final long id, final byte[] data) {
        final WebCommand wc = WebCommand.createWebCommand(wctype, id, data);
        if (wc != null) {
            if (Servers.localServer.LOGINSERVER && wctype != 16 && wctype != 17 && wctype != 19 && wctype != 18 && wctype != 15 && wctype != 20 && wctype != 23 && wctype != 21 && wctype != 25 && wctype != 26 && wctype != 27) {
                Servers.sendWebCommandToAllServers(wctype, wc, wc.isEpicOnly());
            }
            if (WurmId.getOrigin(id) != Servers.localServer.id) {
                Server.getInstance().addWebCommand(wc);
            }
        }
    }
    
    @Override
    public void setKingdomInfo(final int serverId, final byte kingdomId, final byte templateKingdom, final String _name, final String _password, final String _chatName, final String _suffix, final String mottoOne, final String mottoTwo, final boolean acceptsPortals) {
        final Kingdom newInfo = new Kingdom(kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne, mottoTwo, acceptsPortals);
        if (serverId != Servers.localServer.id) {
            Kingdoms.addKingdom(newInfo);
        }
        final WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, kingdomId);
        wck.encode();
        Servers.sendWebCommandToAllServers((short)7, wck, wck.isEpicOnly());
    }
    
    @Override
    public boolean kingdomExists(final int serverId, final byte kingdomId, final boolean exists) {
        WebInterfaceImpl.logger.log(Level.INFO, "serverId:" + serverId + " kingdom id " + kingdomId + " exists=" + exists);
        final boolean result = Servers.kingdomExists(serverId, kingdomId, exists);
        if (Servers.getServerWithId(serverId) != null && Servers.getServerWithId(serverId).name != null) {
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(Servers.getServerWithId(serverId).name) + " kingdom id " + kingdomId + " exists=" + exists);
        }
        else if (Servers.getServerWithId(serverId) == null) {
            WebInterfaceImpl.logger.log(Level.INFO, String.valueOf(serverId) + " server is null " + kingdomId + " exists=" + exists);
        }
        else {
            WebInterfaceImpl.logger.log(Level.INFO, "Name for " + Servers.getServerWithId(serverId) + " server is null " + kingdomId + " exists=" + exists);
        }
        if (Servers.localServer.LOGINSERVER) {
            if (!exists) {
                if (!result) {
                    final Kingdom k = Kingdoms.getKingdomOrNull(kingdomId);
                    boolean sendDelete = false;
                    if (k != null) {
                        if (k.isCustomKingdom()) {
                            k.delete();
                            Kingdoms.removeKingdom(kingdomId);
                            sendDelete = true;
                        }
                    }
                    else {
                        sendDelete = true;
                    }
                    if (sendDelete) {
                        final WcDeleteKingdom wck = new WcDeleteKingdom(WurmId.getNextWCCommandId(), kingdomId);
                        wck.encode();
                        Servers.sendWebCommandToAllServers((short)8, wck, wck.isEpicOnly());
                    }
                }
                else {
                    Servers.sendKingdomExistsToAllServers(serverId, kingdomId, false);
                }
            }
            else {
                Servers.sendKingdomExistsToAllServers(serverId, kingdomId, true);
            }
        }
        return result;
    }
    
    public static void main(final String[] args) {
        if (args.length == 0) {
            try {
                final WebInterfaceTest wit = new WebInterfaceTest();
                wit.shutdownAll("Maintenance restart. Up to thirty minutes downtime.", 600);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            try {
                final WebInterfaceTest wit = new WebInterfaceTest();
                System.out.println("One");
                wit.shutDown(args[0]);
            }
            catch (Exception ex) {
                WebInterfaceImpl.logger.log(Level.INFO, "failed to shut down localhost");
            }
        }
    }
    
    @Override
    public void requestDemigod(final byte existingDeity, final String existingDeityName) {
        final Player[] players = Players.getInstance().getPlayers();
        for (int x = 0; x < players.length; ++x) {
            if (players[x].getKingdomTemplateId() == Deities.getFavoredKingdom(existingDeity) && (players[x].getPower() == 0 || Servers.localServer.testServer)) {
                final MissionPerformer mp = MissionPerformed.getMissionPerformer(players[x].getWurmId());
                if (mp != null) {
                    final MissionPerformed[] perfs = mp.getAllMissionsPerformed();
                    int numsForDeity = 0;
                    WebInterfaceImpl.logger.log(Level.INFO, "Checking if " + players[x].getName() + " can be elevated.");
                    MissionPerformed[] array;
                    for (int length = (array = perfs).length, i = 0; i < length; ++i) {
                        final MissionPerformed mpf = array[i];
                        final Mission m = mpf.getMission();
                        if (m != null) {
                            WebInterfaceImpl.logger.log(Level.INFO, "Found a mission for " + existingDeityName);
                            if (m.getCreatorType() == 2 && m.getOwnerId() == existingDeity) {
                                ++numsForDeity;
                            }
                        }
                    }
                    WebInterfaceImpl.logger.log(Level.INFO, "Found " + numsForDeity + " missions for " + players[x].getName());
                    if (Server.rand.nextInt(numsForDeity) > 2) {
                        WebInterfaceImpl.logger.log(Level.INFO, "Sending ascension to " + players[x].getName());
                        final AscensionQuestion asc = new AscensionQuestion(players[x], existingDeity, existingDeityName);
                        asc.sendQuestion();
                    }
                }
            }
        }
    }
    
    @Override
    public String ascend(final int newId, final String deityName, final long wurmid, final byte existingDeity, final byte gender, final byte newPower, final float initialAttack, final float initialVitality) {
        String toReturn = "";
        if (Servers.localServer.LOGINSERVER) {
            Deity deity = null;
            if (newPower == 2) {
                deity = Deities.ascend(newId, deityName, wurmid, gender, newPower, initialAttack, initialVitality);
                if (deity == null) {
                    return "Ouch, failed to save your demigod on the login server. Please contact administration";
                }
                final StringBuilder builder = new StringBuilder("You have now ascended! ");
                if (initialAttack < 2.0f) {
                    builder.append("The other immortals will not fear your strength initially. ");
                }
                else if (initialAttack < 5.0f) {
                    builder.append("You have acceptable strength as a demigod. ");
                }
                else if (initialAttack < 8.0f) {
                    builder.append("Your strength and skills will impress other immortals. ");
                }
                else {
                    builder.append("Your enormous strength will strike fear in other immortals. ");
                }
                if (initialVitality < 2.0f) {
                    builder.append("You are not the most vital demigod around so you will have to watch your back in the beginning. ");
                }
                else if (initialVitality < 5.0f) {
                    builder.append("Your vitality is acceptable and will earn respect. ");
                }
                else if (initialVitality < 8.0f) {
                    builder.append("You have good vitality and can expect a bright future as immortal. ");
                }
                else {
                    builder.append("Other immortals will envy your fantastic vitality and avoid confrontations with you. ");
                }
                if (deity.healer) {
                    builder.append("Your love and kindness will be a beacon for everyone to follow. ");
                }
                else if (deity.hateGod) {
                    builder.append("Your true nature turns out to be based on rage and hate. ");
                }
                if (deity.forestGod) {
                    builder.append("Love for trees and living things will bind your followers together. ");
                }
                if (deity.mountainGod) {
                    builder.append("Your followers will look for you in high places and fear and adore you as they do the dragon. ");
                }
                if (deity.waterGod) {
                    builder.append("You will be considered the pathfinder and explorer of your kin. ");
                }
                HexMap.VALREI.addDemigod(deityName, deity.number, existingDeity, initialAttack, initialVitality);
                toReturn = builder.toString();
            }
            else if (newPower > 2) {
                String sgender = "He";
                String sposs = "his";
                if (gender == 1) {
                    sgender = "She";
                    sposs = "her";
                }
                Servers.ascend(newId, deityName, wurmid, existingDeity, gender, newPower, initialAttack, initialVitality);
                HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
                Server.getInstance().broadCastSafe(String.valueOf(deityName) + " has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
            }
        }
        else if (newPower > 2) {
            Deities.ascend(newId, deityName, wurmid, gender, newPower, initialAttack, initialVitality);
            String sgender2 = "He";
            String sposs2 = "his";
            if (gender == 1) {
                sgender2 = "She";
                sposs2 = "her";
            }
            HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender2 + " invites you to join " + sposs2 + " religion, as " + sgender2.toLowerCase() + " will now forever partake in the hunts on Valrei!");
            Server.getInstance().broadCastSafe(String.valueOf(deityName) + " has joined the ranks of true deities. " + sgender2 + " invites you to join " + sposs2 + " religion, as " + sgender2.toLowerCase() + " will now forever partake in the hunts on Valrei!");
        }
        return toReturn;
    }
    
    @Override
    public final int[] getPremTimeSilvers(final long wurmId) {
        final PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
        if (info != null) {
            try {
                if (!info.loaded) {
                    info.load();
                }
                if (info.getPaymentExpire() > 0L && info.awards != null) {
                    final int[] toReturn = { info.awards.getMonthsPaidEver(), info.awards.getSilversPaidEver() };
                    return toReturn;
                }
            }
            catch (IOException ex) {}
        }
        return WebInterfaceImpl.emptyIntZero;
    }
    
    @Override
    public void awardPlayer(final long wurmid, final String name, final int days, final int months) {
        Server.addPendingAward(new PendingAward(wurmid, name, days, months));
    }
    
    @Override
    public boolean requestDeityMove(final int deityNum, final int desiredHex, final String guide) {
        if (Servers.localServer.LOGINSERVER) {
            final EpicEntity entity = HexMap.VALREI.getEntity(deityNum);
            if (entity != null) {
                WebInterfaceImpl.logger.log(Level.INFO, "Requesting move for " + entity);
                final MapHex mh = HexMap.VALREI.getMapHex(desiredHex);
                if (mh != null) {
                    entity.setNextTargetHex(desiredHex);
                    entity.broadCastWithName(" was guided by " + guide + " towards " + mh.getName() + ".");
                    entity.sendEntityData();
                    return true;
                }
                WebInterfaceImpl.logger.log(Level.INFO, "No hex for " + desiredHex);
            }
            else {
                WebInterfaceImpl.logger.log(Level.INFO, "Requesting move for nonexistant " + deityNum);
            }
        }
        return false;
    }

    public boolean wuaBan(final String name, String ip, final String reason, final int days) {
    	Player toBan;
    	
		try {
			toBan = Players.getInstance().getPlayer(name);
			
			this.ban(name, reason, days);
			this.addBannedIp(ip, reason, days);
			
			if(toBan.hasLink()) {
	    		toBan.getCommunicator().sendAlertServerMessage("You have been banned for " + days + " days and thrown out from the game.");
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
    
    public boolean wuaMute(final String name, final String reason, final int hours) {
        final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
        Player toMute;
        
        if (info != null) {
            final long expiry = System.currentTimeMillis() + hours * 3600000L;
            try {
                info.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load the player information. Not muted - " + iox.getMessage(), iox);
                return false;
            }
            try {
                info.setMuted(true, reason, expiry);
                toMute = Players.getInstance().getPlayer(name);
                toMute.getCommunicator().sendAlertServerMessage("You have been muted for " + hours + " hours. Reason: " + reason);
               
            }
            catch (NoSuchPlayerException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Not muted - " + iox.getMessage(), iox);
                return false;
            }
            WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Login server muted " + name + ": " + reason + " for " + hours + " hours.");
            return true;
        }
        WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
        return false;
    }
    
    public boolean wuaUnMute(final String name) {
        final PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
        Player toMute;
        
        if (info != null) {
            try {
                info.load();
            }
            catch (IOException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to load the player information. Not unmuted - " + iox.getMessage(), iox);
                return false;
            }
            try {
                info.setMuted(false, "", 0);
                toMute = Players.getInstance().getPlayer(name);
                toMute.getCommunicator().sendAlertServerMessage("You have been unmuted!");
               
            }
            catch (NoSuchPlayerException iox) {
                WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Not unmuted - " + iox.getMessage(), iox);
                return false;
            }
            WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Login server unmuted " + name);
            return true;
        }
        WebInterfaceImpl.logger.warning("Failed to locate the player " + name + ".");
        return false;
    }
    
    public boolean wuaChangePower(final String name, final int gmLevel) {
        Player player;
        
        try {
            player = Players.getInstance().getPlayer(name);
            player.setPower((byte) gmLevel);
            String power = "";
            switch(gmLevel) {
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
           
        }
        catch (NoSuchPlayerException | IOException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Power not changed - " + iox.getMessage(), iox);
            return false;
        }
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Changed powers of " + name + " to " + gmLevel);
        return true;
    }
    
    public boolean wuaChangeKingdom(final String name, final int kingdom) {
    	Player player;
        
        try {
            player = Players.getInstance().getPlayer(name);
            player.setCurrentKingdom(((byte) kingdom));
            player.getKingdomName();
            player.getCommunicator().sendAlertServerMessage("Your kingdom was changed! You are now part of " + player.getKingdomName());
        }
        catch (NoSuchPlayerException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Kingdom not changed - " + iox.getMessage(), iox);
            return false;
        }
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Changed kingdom of " + name + " to " + kingdom);
        return true;
    }

    public boolean wuaGiveItem(final String name, final int itemTemplateID, final float itemQuality, final byte itemRarity, final String creator, final int itemAmount) {
    	Player player;
    	ItemFactory itemFactory = null;
    	Item tempItem = null;
        
        try {
            player = Players.getInstance().getPlayer(name);
            for(int i = 0; i < itemAmount; i++) {
            	tempItem = itemFactory.createItem(itemTemplateID, itemQuality, itemRarity, creator);
            	player.getInventory().insertItem(tempItem);
            }
            player.getCommunicator().sendAlertServerMessage(itemAmount + "x of " + tempItem.getName() + " was added to your inventory");
        }
        catch (NoSuchPlayerException | FailedException | NoSuchTemplateException iox) {
            WebInterfaceImpl.logger.log(Level.WARNING, "Failed to save the player information. Item not added - " + iox.getMessage(), iox);
            return false;
        }
        WebInterfaceImpl.logger.info(String.valueOf(this.getRemoteClientDetails()) + " Added item");
        return true;
    	
    }
}
