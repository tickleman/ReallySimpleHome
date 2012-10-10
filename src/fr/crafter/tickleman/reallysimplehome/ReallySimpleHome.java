package fr.crafter.tickleman.reallysimplehome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

//################################################################################ ReallySimpleHome
public class ReallySimpleHome extends JavaPlugin
{

	public boolean opOnly = true;

	//------------------------------------------------------------------------------------ loadConfig
	public void loadConfig()
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(getDataFolder() + "/config.txt"));
			String buffer = reader.readLine();
			reader.close();
			for (String line : buffer.split("\n")) {
				String[] params = line.split("=");
				if (params.length == 2) {
					String key = params[0];
					String val = params[1];
					if (key.equalsIgnoreCase("oponly")) {
						if (val.equalsIgnoreCase("true")) opOnly = true;
						else if (val.equalsIgnoreCase("false")) opOnly = false;
						else log(Level.WARNING, getDataFolder() + "/config.txt oponly is not true of false");
					}
				}
			}
		} catch (Exception e) {
			log(Level.INFO, "Write default " + getDataFolder() + "/config.txt file");
			saveConfig();
		}
	}

	//------------------------------------------------------------------------------ loadHomeLocation
	public Location loadHomeLocation(Player player)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
				getDataFolder() + "/" + player.getName() + ".txt"
			));
			String buffer = reader.readLine();
			reader.close();
			String[] c = buffer.split(";");
			return new Location(
				getServer().getWorld(c[0]),
				Double.parseDouble(c[1]), Double.parseDouble(c[2]), Double.parseDouble(c[3]),
				Float.parseFloat(c[4]), Float.parseFloat(c[5])
			);
		} catch (Exception e) {
			player.sendMessage("Your home location load failed");
			return player.getLocation();
		}
	}

	//------------------------------------------------------------------------------------------- log
	public void log(Level level, String message)
	{
		getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + message);
	}

	//------------------------------------------------------------------------------------- onCommand
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (cmd.getName().toLowerCase().equalsIgnoreCase("home")) {
			if (!opOnly || sender.isOp()) {
				if (args.length == 0 && sender instanceof Player) {
					// go home
					Player player = (Player)sender;
					sender.sendMessage("Go home");
					player.teleport(loadHomeLocation(player));
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("set") && sender instanceof Player) {
						// set home
						Player player = (Player)sender;
						saveHomeLocation(player);
						player.sendMessage("Home set");
					} else if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
						// reload configuration
						sender.sendMessage("Reload homes global configuration");
						loadConfig();
						String[] newArgs = {"config"};
						onCommand(sender, cmd, commandLabel, newArgs);
					} else if (args[0].equalsIgnoreCase("config") && sender.isOp()) {
						// display all configuration options values
						String[] newArgs = {"config", "oponly"};
						onCommand(sender, cmd, commandLabel, newArgs);
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("config") && sender.isOp()) {
						// display configuration options value
						if (args[1].equalsIgnoreCase("oponly")) {
							sender.sendMessage("- opOnly = " + (opOnly ? "true" : "false"));
						}
					}
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("config") && sender.isOp()) {
						// set configuration options
						if (args[1].equalsIgnoreCase("oponly")) {
							if (args[2].equalsIgnoreCase("true")) {
								opOnly = true;
								saveConfig();
							} else if (args[2].equalsIgnoreCase("false")) {
								opOnly = false;
								saveConfig();
							}
						}
						String[] newArgs = {"config", args[1]};
						onCommand(sender, cmd, commandLabel, newArgs);
					}
				}
			}
			return true;
		} else if (
			cmd.getName().toLowerCase().equalsIgnoreCase("homeset")
			|| cmd.getName().toLowerCase().equalsIgnoreCase("sethome")
		) {
			if (!opOnly || sender.isOp()) {
				// set home
				Player player = (Player)sender;
				saveHomeLocation(player);
				player.sendMessage("Home set");
			}
			return true;
		}
		return false;
	}

	//------------------------------------------------------------------------------------- onDisable
	@Override
	public void onDisable()
	{
	}

	//-------------------------------------------------------------------------------------- onEnable
	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		loadConfig();
	}

	//------------------------------------------------------------------------------ saveHomeLocation
	public void saveConfig()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(getDataFolder() + "/config.txt"));
			writer.write("oponly=" + (opOnly ? "true" : "false"));
			writer.close();
		} catch (Exception e) {
			log(Level.SEVERE, "Could not save " + getDataFolder() + "/config.txt");
		}
	}

	//------------------------------------------------------------------------------ saveHomeLocation
	public void saveHomeLocation(Player player)
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
				getDataFolder() + "/" + player.getName() + ".txt"
			));
			Location location = player.getLocation();
			writer.write(
				location.getWorld().getName() + ";"
				+ location.getX() + ";" + location.getY() + ";" + location.getZ() + ";"
				+ location.getYaw() + ";" + location.getPitch()
			);
			writer.close();
		} catch (Exception e) {
			log(Level.SEVERE, "Could not save " + getDataFolder() + "/" + player.getName() + ".txt");
			player.sendMessage("Your home location save failed");
		}
	}

}
