import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class Homes extends Plugin {

	private final static Logger log = Logger.getLogger("Minecraft");
	private static String name = "Homes";

	public void initialize() {
		PluginListener listener = new HomesListener();
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.LOW);
		log.info(name + " initialized");
	}

	public void disable() {
		etc.getInstance().removeCommand("/homes");
		log.info(name + " disabled");
	}

	public void enable() {
		etc.getInstance().addCommand("/homes", "- multihome support.");
		log.info(name + " enabled");
	}

	private class HomesListener extends PluginListener {
		public boolean onCommand(Player player, String[] split) {
			if (split[0].equalsIgnoreCase("/homes") && player.canUseCommand(split[0])) {
				boolean usage = false;
				
				String name = "";
				if (split.length >= 3) {
					for (int i = 2; i < split.length; i++ )
						name += split[i] + " ";
					
					name = name.trim();
				}
				
				if (split.length >= 3 && name.length() > 0) {
					if (split[1].equalsIgnoreCase("add")) {
						Connection conn = etc.getSQLConnection();
						PreparedStatement st = null;
						try {
							st = conn.prepareStatement("INSERT INTO savehomes (name, x, y, z, rotX, rotY, beschrijving) VALUES (?,?,?,?,?,?,?)");
							st.setString(1, player.getName());
							st.setDouble(2, player.getX());
							st.setDouble(3, player.getY());
							st.setDouble(4, player.getZ());
							st.setFloat(5, player.getRotation());
							st.setFloat(6, player.getPitch());
							st.setString(7, name);
							st.executeUpdate();

							player.sendMessage(Colors.Green + "Home '" + name + "' saved.");
						} catch (Exception e) {
							log.info("homes: Error saving '" + name + "' of player '" + player.getName() + "' error: ");
							e.printStackTrace();
						} finally {
							try {
								if (conn != null)
									conn.close();
								if (st != null)
									st.close();
							} catch (Exception e) {
							}
						}
					} else if (split[1].equalsIgnoreCase("del")) {
						Connection conn = etc.getSQLConnection();
						PreparedStatement st = null;
						ResultSet rs = null;
						try {
							st = conn.prepareStatement("SELECT beschrijving FROM savehomes WHERE name = ? AND beschrijving LIKE ? ORDER BY beschrijving");
							st.setString(1, player.getName());
							st.setString(2, name + "%");
							rs = st.executeQuery();
							
							if (rs.next()) {
								if (rs.isLast() || rs.getString(1).equalsIgnoreCase(name)) {
									name = rs.getString(1);
									rs.close();
									st.close();
									
									st = conn.prepareStatement("DELETE FROM savehomes WHERE name = ? AND beschrijving = ?");
									st.setString(1, player.getName());
									st.setString(2, name);
									st.executeUpdate();
		
									player.sendMessage(Colors.Green + "Home '" + name + "' deleted.");
								} else {
									
									String msg = Colors.Rose + "Found multiple results: " + rs.getString(1);
									while (rs.next())
										msg += ", " + rs.getString(1);

									player.sendMessage(msg);
								}
							} else {
								player.sendMessage(Colors.Rose + "No home found for: " + name);
							}
						} catch (Exception e) {
							log.info("homes: Error deleting '" + name + "' of player '" + player.getName() + "' error: ");
							e.printStackTrace();
						} finally {
							try {
								if (conn != null)
									conn.close();
								if (st != null)
									st.close();
								if (rs != null)
									rs.close();
							} catch (Exception e) {
							}
						}
					} else if (split[1].equalsIgnoreCase("use")) {
						Connection conn = etc.getSQLConnection();
						PreparedStatement st = null;
						ResultSet rs = null;
						try {
							st = conn.prepareStatement("SELECT beschrijving, x, y, z, rotX, rotY FROM savehomes WHERE name = ? AND beschrijving LIKE ? ORDER BY beschrijving");
							st.setString(1, player.getName());
							st.setString(2, name + "%");
							rs = st.executeQuery();

							if (rs.next()) {
								if (rs.isLast() || rs.getString(1).equalsIgnoreCase(name)) {
									Warp home = new Warp();
									home.Location = new Location(rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getFloat(5), rs.getFloat(6));
									home.Group = "";
									home.Name = player.getName();
									etc.getInstance().changeHome(home);
									player.sendMessage(Colors.Green + "Home '" + rs.getString(1) + "' loaded. It's your /home now!");
								} else {
									String msg = Colors.Rose + "Found multiple results: " + rs.getString(1);
									while (rs.next())
										msg += ", " + rs.getString(1);

									player.sendMessage(msg);
								}
							} else {
								player.sendMessage(Colors.Rose + "No home found for: " + name);
							}
						} catch (Exception e) {
							log.info("homes: Error using '" + name + "' of player '" + player.getName() + "' error: ");
							e.printStackTrace();
						} finally {
							try {
								if (conn != null)
									conn.close();
								if (st != null)
									st.close();
								if (rs != null)
									rs.close();
							} catch (Exception e) {
							}
						}
					} else {
						usage = true;
					}
				} else if (split.length == 2 && split[1].equalsIgnoreCase("list")) {
					Connection conn = etc.getSQLConnection();
					PreparedStatement st = null;
					ResultSet rs = null;
					try {
						st = conn.prepareStatement("SELECT beschrijving FROM savehomes WHERE name = ? ORDER BY beschrijving");
						st.setString(1, player.getName());
						rs = st.executeQuery();

						String msg = Colors.Green;
						if (rs.next()) {
							msg += "Your homes: " + Colors.White + rs.getString(1);
							
							while (rs.next())
								msg += ", " + rs.getString(1);
							
						} else {
							msg += "No homes found";
						}
						player.sendMessage( msg );
					} catch (Exception e) {
						log.info("homes: Error listing homes of player '" + player.getName() + "' error: ");
						e.printStackTrace();
					} finally {
						try {
							if (conn != null)
								conn.close();
							if (st != null)
								st.close();
							if (rs != null)
								rs.close();
						} catch (Exception e) {
						}
					}
				} else {
					usage = true;
				}

				if (usage)
					player.sendMessage(Colors.Rose + "usage: /homes add <alias> | del <alias> | use <alias> | list");

				return true;
			}
			return false;
		}
	}
}
