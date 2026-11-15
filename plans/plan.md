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
## Villager Experience and Leveling

Villagers can now gain experience and level up through trading and production activities. The XP requirements per level are configurable per profession through an exponential progression system.

### XP Progression

Each profession can define its own XP curve using the following parameters:

- **base_xp**: The minimum XP required to reach level 2 (default: 100)
- **exponent**: The exponential multiplier for each subsequent level (default: 1.5)
  - An exponent of 1.5 means each level requires 1.5x more XP than the previous level
  - An exponent of 2.0 would create a quadratic progression
  - Lower values (e.g., 1.2) create a more linear progression

### XP Formula

For a given level `n` (where level 1 is the starting level):

```
xp_required_for_level_n = base_xp * (exponent ^ (n - 2))
```

For example, with `base_xp=100` and `exponent=1.5`:
- Level 1: 0 XP (starting level)
- Level 2: 100 XP
- Level 3: 150 XP (100 * 1.5)
- Level 4: 225 XP (150 * 1.5)
- Level 5: 337.5 XP (225 * 1.5)

### Configuration

XP progression is configured in the workshop definition JSON:

```json
{
  "profession": "farmer",
  "xp_progression": {
    "base_xp": 100,
    "exponent": 1.5,
    "max_level": 5
  },
  "produce": [...]
}
```

If not specified, default values will be used:
- `base_xp`: 100
- `exponent`: 1.5
- `max_level`: 5

### XP Gain

Villagers gain experience from:
1. **Trading**: When a player trades with a villager
2. **Production**: When a villager successfully produces an item

The amount of XP gained depends on the complexity of the trade or item produced (to be configured per trade/item).

### Level Benefits

Higher levels provide the following benefits:
1. **Production Unlock**: Certain items can only be produced at specific villager levels
2. **Trade Availability**: Higher-tier trades become available at higher levels
3. **Efficiency**: Higher-level villagers may produce items faster or with better quality
4. **Repair Services**: Special NPC services (repairs, healing, etc.) have increased effectiveness at higher levels

## Player Reputation System (DETAILED)

### Overview

Players have a reputation with each village that ranges from -1.0 (hostile) to +1.0 (friendly), with 0.0 being neutral. Reputation is tracked per-village, meaning a player can be beloved by one village while hostile to another.

### Reputation Levels

| Range | Level | Villager Behavior | Golem Behavior | Trading |
|-------|-------|-------------------|-----------------|---------|
| -1.0 to -0.5 | Hostile | Flee on sight | Attacks player | Blocked |
| -0.5 to 0.0 | Unfriendly | Avoid player | Neutral | Reduced |
| 0.0 | Neutral | Normal | Neutral | Normal |
| 0.0 to 0.5 | Agreeable | Neutral, might help | Neutral | Normal |
| 0.5 to 1.0 | Friendly | Approach player | Help player | Discounted |
| 1.0 | Beloved | Celebrate arrival | Loyal defender | Maximum discount (50%) |

### Events That Affect Reputation

#### Negative Events (Decrease Reputation)

- **Villager Killed** (-0.20): Player kills a villager in the village
- **Villager Bed Stolen** (-0.10): Player forces a villager out of bed
- **Workstation Destroyed** (-0.10): Player breaks a workstation
- **Chest Looted** (-0.05): Player opens/loots village chests
- **Workshop Invalidated** (-0.05): Villager fails workshop validation due to player actions
- **Villager Hit** (-0.03): Player hits a villager
- **Block Broken** (-0.02): Player breaks blocks in village territory

#### Positive Events (Increase Reputation)

- **Raid Defeated** (+0.20): Player defeats a raid defending this village
- **Resource Donated** (+0.10): Player donates items to village (future feature)
- **Workstation Repaired** (+0.10): Player repairs a damaged workstation
- **Task Completed** (+0.15): Player completes a village task/job (future system)
- **Emergency Aid Provided** (+0.15): Player provides supplies during crisis
- **Trade Completed** (+0.01): Player trades with a villager

### Trade Price Modifier

For reputations above 0.5, each 0.1 provides 10% discount up to maximum 50% discount at reputation 1.0:

- At 0.5: 1.0x (no discount)
- At 0.6: 0.9x (10% discount)
- At 0.7: 0.8x (20% discount)
- At 0.8: 0.7x (30% discount)
- At 0.9: 0.6x (40% discount)
- At 1.0: 0.5x (50% discount)

This applies to both buying and selling: discounts when buying, higher value when selling.

### Reputation Effects

#### Trading (Reputation ≥ -0.5)
- Negative reputation (-0.5 to 0.0): No special services
- Neutral (0.0): Normal trading
- Positive (>0.0): Trade discounts apply

#### Services (Reputation > 0.0)
- Special services (repair, healing, etc.) only available with positive reputation
- Quality of service may improve with higher reputation

#### Villager Behavior (Reputation < -0.5)
- Villagers flee from player on sight
- Villagers take longer routes to avoid player
- May not trade at all, even if reputation > -0.5

#### Iron Golem Behavior (Reputation < -0.5)
- Golems attack hostile players
- Golems target player as enemy
- Golems do not provide protection

### Configuration

Reputation changes are configurable per-village through data pack:

```json
{
  "reputation_events": {
    "villager_killed": -0.20,
    "trade_completed": 0.01,
    ...
  }
}
```

Modpack authors can adjust:
- How much each event affects reputation
- Overall balance of positive vs negative
- Recovery difficulty
- Consequences of negative reputation

### Persistence

- Reputation stored in player NBT data
- Saved per-village with key: `VWAM_Rep_{villageId}`
- Survives world saves and server restarts
- Different worlds maintain separate reputations
- Cannot be shared between players

### Recovery Mechanics

With default settings:
- **Quick recovery**: 20 trades (normal rate) to recover from 1 villager kill
- **Medium recovery**: 2 donations to recover from 1 villager kill
- **Task recovery**: ~1.3 tasks to recover from 1 villager kill (future)
- **Prevention**: Avoid harming villagers and their property

### Future Extensions

Reputation system foundation for:
1. **Task System**: Players employed by village earn reputation
2. **Faction Reputation**: Reputation shared among aligned villages
3. **Banishment**: Permanent exile at extreme negative reputation
4. **Amnesty**: Special events that reset reputation
5. **Reputation Tiers**: Unlock content/quests at reputation milestones
