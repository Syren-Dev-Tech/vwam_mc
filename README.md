# Villagers, Workshops, and Markets

The goal of this mod is to incentivise players to improve villages, while restricting the "min-max" meta of trading halls and villager farms.
It expands upon the requirements for villagers to trade, introduces a market system for villages, and balances the economics of player-villager economics.

Too, this will remove unfair or imbalanced mechancs, replacing them with friendlier, more immersive ones.

## Workshops

What defines a valid workshop:

1. There is an accessable workstation available (vanilla requirement)
2. There are 3 "support" workstations available, depending on the workstation, such as:
    - Crafting tables
    - Furnaces
    - Campfires
3. There is nearby storage, such as:
    - Single chests
    - Double chests
    - Barrels
4. The distance from the workstation to any "support" workstation is less than 9 blocks away
5. The distance from the workstation to any storage is less than 9 blocks away
6. There is a 1 block radius around the workstation (3x3 area with the workstation in the center) of free space
7. All spots in the above 3x3 area have a light level greater than 9
8. There cannot be another identical workstation within 9 blocks

For rules that limit the distance of things, such as "support" workstations being 3 blocks away at most, they can be +- 1 block to the workstation, meaning some vertical build configuration is acceptable (especially for storage).
However, for each of these required blocks, there must be valid pathfinding from the workstation to that block, meaning players cannot hide them all in one spot.

Workstations, as long as they are far enough apart, can share these "support" workstations and storages.

### Workshop Quality

The "quality" of a workshop will determine the "quality of work" of the villager.

By default, assuming a base rate of 1 item produced per cycle per day, a workshop can be affected with the following quality ratings:

- Cramped:
    - The average distance for all "support" workstations is less than or equal to 3
    - Production rate is set at 0.5
- Tight:
    - The average distance for all "support" workstations is less than or equal to 4
    - Production rate is set at 0.75
- Normal:
    - The average distance for all "support" workstations is less than or equal to 5
    - Production rate is set at 1
- Roomy:
    - The average distance for all "support" workstations is less than or equal to 6
    - Production rate is set at 1.25
- Spacious:
    - The average distance for all "support" workstations is greater than 6
    - Production rate is set at 1.5

### Workshop Beauty

There is also a "beauty" of a workshop that will affect the "trade discount" of the villager, for the following ratings:

- Modest:
    - Score less than 1
    - Trade rates are 1x
- Elegant:
    - Score less than 1.5
    - Trade rates are 1.1x
- Striking:
    - Score less than 2
    - Trade rates are 1.2x
- Magnificent:
    - Score less than 2.5
    - Trade rates are 1.3x 
- Sublime:
    - Score less than 3
    - Trade rates are 1.4x
- Transcendent:
    - Score greater than 3
    - Trade rates are 1.5x

Trade rates are applied as the amount required to pay for buying an item, or the amount of items required for a sale.
For example, to purchase 10 carrots for 1 emerald, a rate of 1 would be 10:1.
At a rate of 1.5, it becomes 15:1.
The quantity of emeralds does not change.

This is calculated using: [product]*[rate] -> [emeralds]

For selling, this is the same, but in the reverse.
To sell 10 carrots for 1 emerald, a rate of 1 would be 1:10 (1 emerald per 10 sold).
At a rate of 1.5, it becomes 7:1 (1 emerald per 7 sold).

This is calculated using: [product]/[rate] -> [emeralds]

This beauty score is determined by the number of "decorations" in the workshop area.
This can include:

- Potted plants: 0.3 each
- Paintings: 0.2 each
- Carpets: 0.05 each
- Colored glass/panes: 0.08 each
- Hanging signs: 0.2 each
- Lanterns: 0.2 each

## Routines

Vanilla villager daily schedules are:

- 0600: wake up and wander
- 0800: work and restock
    - Can restock twice per day
- 1500: social gathering
- 1800: sleep

Villager routines will be modified to include the following:

1. The first time a villager accesses their workstation for the day will run the "is valid workshop" check
    - Invalid workshops will set the villager as "unable to work" until the next day
    - Valid workshops will set the villager as "able to work" for the day
    - As long as the villager is marked "able to work," it is able to trade and produce products
2. Villagers will receive a status "rested" when they sleep in a bed
    - Villagers without a "rested" status will not produce new materials, but will allow trading
3. Villagers will receive a "socialized" status when they social gather (vanilla mechanic)
    - Villagers without a "socialized" status will not trade, but will produce new materials

## Work and Trade Behaviour

We are replacing the vanilla routine of a villager restocking their inventory for trades with the following:

- Villagers must be bound to villages to trade, meaning even if they have a valid profession, players cannot interact with them for trading
- Villages have a common "stockpile" for trading
    - This is a virtual stockpile, where the mod keeps track of what stock is available at what price for each village
- Villagers of a common profession (ie, all farmers) will offer the same trades, but only the items that match their profession
    - No more mix of 1 villager buying wheat while another only buys carrots
- Villages will have a "supply and demand" modifier for its stockpile
    - If the stock of a specific item falls below a certain amount, its price will increase
    - If the stock of a specific item is over a certain amount, its value will decrease
    - Intent: if a player buys all the carrots from a village, the price of purchasing more carrots will be higher than baseline until the village "restocks" to its baseline amount
        - The flip-side of this is the value of selling carrots to the village is increased, meaning the player can sell carrots for more
    - Intent: if a player dumps all their carrots into a village by sale, the value of carrots will decrease until the number of carrots in the village falls back to a "reasonable" amount
        - The flip-side of this is the price of purchasing carrots from the village decreases, meaning the player can purchase more carrots for less
- Villagers will produce items in a "round-robin" order with the following restrictions:
    - They must have the necessary ingredients (in the village stockpile) to craft that item
    - There must be be fewer than the "reasonable" amount of that item already in the village stockpile
    - The villager must be high enough level to sell that item to produce that item
        - A "base level" villager cannot produce the "highest trade" value items
    - If the item cannot be created, or there is enough of it, the next item in the list will be used, until the list is exhausted
- Villagers can gain experience either by trading with players or by producing items

The following mechanics will also be modified:

- Player buffs like "hero of the village" will apply a buff "met the hero" to villagers traded with
    - Lasts 24 in-game hours
    - Increases productivity of villager by 2x
    - Only applies to village that was part of the raid event that awarded the player "hero of the village"

## Reputation

Players will have a reputation with each village.
Reputation ranges from -1 to 1, where -1 is hostile, 0 is neutral, and 1 is friendly.

This reputation / karma score is affected by:

- Killing a villager that is not part of a village
    - No reputation change
- Killing a villager that is part of a village
    - Negative reputation change
- Trading with a villager
    - Positive reputation change
- Defeating a village raid
    - Positive reputation change

Reputation determines whether a village will trade with a player, such that a negative score denies trading outright.
If the reputation falls below -0.5, villagers will run from the player, and iron golems will attack the player.

However, having a score greater than 0.5 will apply a 10% reduction in trade prices for each step of 0.1 until 1, such that a reputation of 1 or less is 50%, 0.9 or less is 40%, 0.8 or less is 30%, 0.7 or less is 20%, and 0.6 or less (to 0.5) is 10%.

## Trade and Produce Balances

Villagers will offer to purchase or produce items they can theoretically craft, meaning enchanted items (and books) will not be obtainable via trading.

Instead, villagers will offer the following services given the player has a positive (greater than 0) reputation:

- Armourer:
    - Repair piece of armour
        - Amount of repair depends on level
        - Costs emeralds
        - Every 24 in game hours
- Cleric:
    - Heal the player
        - Amount of healing depends on level
        - Free
        - Every 12 in game hours
    - Remove status effects
        - Free
        - Every 24 in game hours
- Fisherman:
    - Offers to "take player fishing"
        - Free
        - Random chance of getting bucket of fish or axolotl
        - Every 48 in game hours
- Fletcher:
    - Repair bow or crossbow
        - Amount of repair depends on level
        - Costs emeralds
        - Every 24 in game hours
- Librarian:
    - Book exchange
        - Takes an enchanted book and swaps it for a random one
        - Costs emeralds
        - Every 72 in game hours
- Toolsmith
    - Repair tool
        - Amount of repair depends on level
        - Costs emeralds
        - Every 24 in game hours
- Weaponsmith
    - Repair sword or axe
        - Amount of repair depends on level
        - Costs emeralds
        - Every 24 in game hours

### Wandering Traders

Wandering traders, instead of being completely isolated nomads, will instead be "bound" to known villages.
This means until a player discovers or interacts with a village, no wandering trader will spawn.

The trades of the wandering villager will be randomly selected from the stockpile of their village, but will offer to purchase things otherwise ignored by villagers.

For example, the wandering trader may offer to purchase:

- Coral blocks
- Flowers
- Saplings
- Dyes

(These are things they would normally sell in vanilla)
