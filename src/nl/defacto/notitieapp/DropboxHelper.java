package nl.defacto.notitieapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

public class DropboxHelper {
	static final int LINK_DB = 1;
	
	private static String access_token;
	
	private DbxAccountManager mDbxAcctMgr;
	
	public DropboxHelper(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences("oauth", Activity.MODE_PRIVATE);
		access_token = preferences.getString("access_token", null);		
		
		if(access_token == null) {
			Intent intent = new Intent(activity, OAuthActivity.class);
			activity.startActivity(intent);
		}
	}
	
	public List<DbxFileInfo> fetchNotes() throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		return dbxFs.listFolder(new DbxPath(""));
	}
	
	public void saveNote(String title, String body) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.create(new DbxPath(title + ".md"));
		
		try {
		    testFile.writeString(body);
		} finally {
		    testFile.close();
		}
	}
	
	public void updateNote(String title, String body) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.open(new DbxPath(title + ".md"));
		
		try {
		    testFile.writeString(body);
		} finally {
		    testFile.close();
		}
	}
	
	public String loadNote(String note) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.open(new DbxPath(note + ".md"));
		String content = null;
		
		try {
		    content = testFile.readString();
		} finally {
		    testFile.close();
		}
		
		return content;
	}
	
	public void deleteNote(String note) throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		dbxFs.delete(new DbxPath(note + ".md"));
	}
	
	public boolean noteExists(String note) throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		return dbxFs.exists(new DbxPath(note + ".md"));
	}
}
