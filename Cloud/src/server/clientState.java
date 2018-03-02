package server;

import java.io.Serializable;
import java.rmi.Remote;

public enum clientState implements Remote, Serializable {
	SUCCESS, 
	WRONG_PASSWORD, 
	USERNAME_UNAVAILABLE, 
	ALREADY_LOGGED, 
	ALREADY_REGISTERED, 
	NOT_REGISTERED, 
	SERVER_DOWN
}
