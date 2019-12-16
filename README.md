# DeathSwapUnlimited
SpigotMC plugin for a DeathSwap gamemode with unlimited players.<br>

### Permissions
To start/stop a DeathSwap game using /deathswap, the player needs `deathswap.toggle`.<br>
To toggle switching to spectator upon dying (/specondeath), the player needs `deathswap.spectateondeath`<br>

### TO-DO
`-` Upload to SpigotMC<br>
`-` Make a config file for users to adjust certain settings.<br>

### Known Bugs
(These are easy fixes and I will fix them when I get a chance)<br>
`-` "Overheal" visual bug with the boss bars for final 2 players. If a player, for example, uses a health potion that would heal more than what they need to heal, their health on the top bar "overheals." I.E. if they're at 19hp and heal for 3hp, the bar would read 22hp.<br>
`-` Ocean spawning | The initial random teleportation has a good chance of spawning in the ocean. When I make a config file, I'll add an option to avoid this and have it enabled by default.<br>
`-` Permanent Invulnerability | During the initial random teleportation, players are given 10 seconds of invulnerability followed by a full heal. If a player disconnects during this time then rejoins after the heal, they will keep their invulnerability.<br>
