package user;

public class User implements UserInterface {
	private static final long serialVersionUID = 1L;
	//VARIABILI
	private String username;
	private String password;
  
	public User(String username, String password){
		this.username = username;
		this.password = password;	
	}
	
	@Override
	public boolean equals(Object x){
		if(this == x)
			return true;
		if(!(x instanceof User))
			return false;
		User aux = (User) x;
		return aux.getUsername().equals(this.getUsername()); 
	}
	
	@Override
	public String toString() {
		return getUsername()+" "+getPassword();
	}
	
	@Override
	public int hashCode() {
		return this.getUsername().hashCode();
	}

	//GETTER
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}	
}
