This repository is now archived an not further maintained.

# Simple tpa plugin for minecraft
I wrote this plugin without any good knowledge of either java nor bukkit/spigot. A short description of the functionality is added. 

## Commands
The plugin implements basic commands, each command needs permissions.

| command | arguments | description | permission |
| --- | --- | --- | --- |
| `tptoggle` | None | Should toggle tp requests, not tested | `tpa.off` |
| `back` | None | Return back to location before teleport, or before death | `tpa.back` or `tpa.backondeath` |
| `tpa` | player_name | Ask to tp to a given player | `tpa.tpa` |
| `tpask` | player_name | Ask to tp to a given player | `tpa.tpa` |
| `tpaccept` | [player_name] | Accept a tp, a player can be specified | `tpa.tpa` |
| `tpyes` | [player_name] | Accept a tp request, a player can be specified | `tpa.tpa` |
| `tpdeny` | [player_name] | Deny a tp request, a player can be specified | `tpa.tpa` |
| `tpno` | [player_name] | Deny a tp request, a player can be specified | `tpa.tpa` |

If a player has the permission `tpa.backondeath`, or `tpa.back`, the player can use `/back` when they died to return to the location where they died.
