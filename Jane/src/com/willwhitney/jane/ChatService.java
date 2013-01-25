package com.willwhitney.jane;

/*
---------------------------------------------------------------
--------------------- DEPRECATED ------------------------------
---------------------------------------------------------------
public class ChatService extends Service {

	public final static int LOGIN_FAILED = 0;
	public final static int LOGIN_SUCCESSFUL = 1;
	public final static String NEW_MESSAGE = "jane.xmpp.NEW_MESSAGE";
	public final static String NEW_CHAT = "jane.xmpp.NEW_CHAT";
	public final static String SET_ACTIVE_CHAT = "jane.xmpp.SET_ACTIVE_CHAT";
	public final static String CHAT_RESPONSE = "jane.xmpp.CHAT_RESPONSE";

	private Messenger uiMessenger;
	private XMPPConnection connection;

	private ChatServiceListener chatServiceListener;
	private Map<String, String> nameCache;

	protected Chat activeChat;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {

		String username = intent.getExtras().getString("username");
		String password = intent.getExtras().getString("password");
		uiMessenger = intent.getExtras().getParcelable("messenger");

		nameCache = new HashMap<String, String>();

		org.jivesoftware.smack.SmackAndroid.init(this);

		LoginThread login = new LoginThread(username, password, this);
		login.start();

		return START_STICKY;
	}

	public void loginCallback(android.os.Message result, XMPPConnection connection) {
		try {
			uiMessenger.send(result);
		} catch (RemoteException re) {
			re.printStackTrace();
		}

		this.connection = connection;
		activeChat = null;
		chatServiceListener = new ChatServiceListener(this);

		connection.getChatManager().addChatListener(chatServiceListener);

		JaneService.localBroadcastManager.registerReceiver(JaneService.localChatReceiver,
				new IntentFilter(NEW_MESSAGE));
		JaneService.localBroadcastManager.registerReceiver(chatServiceListener,
				new IntentFilter(CHAT_RESPONSE));
		JaneService.localBroadcastManager.registerReceiver(chatServiceListener,
				new IntentFilter(SET_ACTIVE_CHAT));
	}

	//TODO: figure out what determines the presence of a RosterEntry's name field
	//TODO: Intent-based race condition. probably shouldn't send message if "tell X" fails
	public void setActiveChatByName(String name) {
		Roster roster = connection.getRoster();
		for(RosterEntry entry : roster.getEntries()) {
			String potentialName = entry.getName();
			Log.i("Chat", "Checking desired recipient: " + name + " against: " + potentialName);
			if(potentialName != null && potentialName.regionMatches(true, 0, name, 0, name.length())) {
				Log.i("Chat", "Setting active chat to " + potentialName);
				activeChat = connection.getChatManager().createChat(entry.getUser(), null);
				return;
			} else if(entry.getUser().contains(name)) { //maybe emails will be ok...
				Log.i("Chat", "Setting active chat to " + entry.getUser());
				activeChat = connection.getChatManager().createChat(entry.getUser(), null);
				return;
			}
		}
	}

	public String getNameForEmail(String email) {
		Log.i("Chat", "Getting name for " + email);
		if(nameCache.containsKey(email)) {
			return nameCache.get(email);
		} else {
			Roster roster = connection.getRoster();
			for(RosterEntry entry : roster.getEntries()) {
				Log.i("Chat", entry.getUser() + "," + entry.getName());
				if(entry.getUser().equals(email)) {
					String name = entry.getName();
					Log.i("Chat", "Got name for " + email + " as " + name);
					if(name == null || name.equals("")) {
						return email;
					} else {
						nameCache.put(email, name);
						return name;
					}
				}
			}
			return email;
		}
	}
}
*/
