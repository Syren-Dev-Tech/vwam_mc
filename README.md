# Villager Workshops

A NeoForge 1.21.1 mod that enables players to create custom villager professions and workstations entirely through data packs and resource packs. No coding required!

## ✨ Features

- **Data Pack Support**: Define custom professions in simple JSON files
- **Custom Workstations**: Create profession-specific workstation blocks
- **Resource Pack Integration**: Apply custom skins and textures via resource packs
- **User-Friendly**: No code modifications needed—extend the system purely through packs
- **Level Support**: Professions can have different variants for different levels
- **Easy Integration**: Works seamlessly with existing Minecraft content

## 🚀 Quick Start

See [QUICKSTART.md](QUICKSTART.md) for a 5-minute setup guide.

## 📖 Documentation

- **[WIKI.md](WIKI.md)** - Comprehensive documentation
- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute setup guide
- **[examples/](examples/)** - Example data packs and resource packs

## 📋 System Requirements

- Minecraft 1.21.1
- NeoForge 1.21.1+
- Java 21+

## 🎮 How It Works

### Data Packs

Define custom professions:
```json
{
  "workstation_block": "minecraft:lectern",
  "display_name": "Lore Master",
  "level": 3,
  "enabled": true
}
```

### Resource Packs

Apply custom textures:
```
assets/villager_workshops/textures/entity/villager/profession/
└── yournamespace/
    ├── profession_name.png
    └── profession_name_level_2.png
```

### Commands

Apply professions to villagers:
```
/summon minecraft:villager ~ ~ ~ {CustomProfession:"yournamespace:profession_name"}
/data modify entity @s CustomProfession set value "yournamespace:profession_name"
```

## 📁 File Structure

### For Users (Data Pack Format)

```
datapack_name/
├── pack.mcmeta
└── data/
    └── your_namespace/
        └── villager_workshops/
            ├── professions/
            │   └── your_profession.json
            └── workstations/
                └── your_workstation.json
```

### For Resource Pack Artists

```
resourcepack_name/
├── pack.mcmeta
└── assets/
    └── villager_workshops/
        └── textures/
            └── entity/
                └── villager/
                    └── profession/
                        └── your_namespace/
                            └── your_profession.png
```

## 🔧 Installation

1. Download the mod JAR file
2. Place it in `.minecraft/mods/` (NeoForge must be installed)
3. Launch Minecraft
4. Create or install data packs with custom professions

## 📚 API Documentation

### For Mod Developers

```java
// Access the registry
CustomRegistryManager registry = RegistryHandler.getInstance();

// Get a profession
Optional<CustomProfession> profession = registry.getProfession(
    new ResourceLocation("namespace", "profession_name")
);

// Apply profession to villager
VillagerEvents events = new VillagerEvents();
events.applyCustomProfession(villager, professionId);

// Get profession info from villager
ResourceLocation professionId = VillagerEvents.getCustomProfessionId(villager);
String displayName = VillagerEvents.getCustomProfessionDisplayName(villager);
```

## 🎨 Creating Content

### Profession Definition Schema

```json
{
  "workstation_block": "string (required) - ResourceLocation of workstation block",
  "display_name": "string (optional) - Human-readable name",
  "level": "integer 1-5 (optional) - Profession level, defaults to 1",
  "enabled": "boolean (optional) - Whether enabled, defaults to true"
}
```

### Workstation Definition Schema

```json
{
  "block": "string (required) - ResourceLocation of the block",
  "display_name": "string (optional) - Human-readable name",
  "is_poi": "boolean (optional) - Register as Point of Interest, defaults to true"
}
```

## 🖼️ Texture Guidelines

- **Size**: 64x64 pixels (standard) or 128x128 pixels (HD)
- **Format**: PNG with transparency
- **Naming**: Follow `{profession}` or `{profession}_level_{1-5}` pattern
- **Colors**: Use standard Minecraft villager color schemes

## 🐛 Troubleshooting

### Data Pack Not Loading

```
# Verify data pack is installed
/datapack list

# Reload data packs
/reload

# Check logs for errors
```

### Profession Not Applying

- Verify profession ID is spelled correctly
- Ensure villager entity is selected
- Check that NBT tag syntax is correct

### Texture Not Showing

- Verify resource pack is enabled
- Check file path matches profession ID
- Ensure PNG file is valid
- Reload resources with `F3 + T`

See [WIKI.md](WIKI.md#troubleshooting) for more troubleshooting tips.

## 🔮 Future Roadmap

- [ ] Trading recipe definitions via data packs
- [ ] Custom villager behaviors
- [ ] Integration with more blocks
- [ ] GUI for profession selection
- [ ] Custom sounds and animations
- [ ] Multiplayer sync improvements

## 📝 License

[Specify your license here]

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 💬 Support

For issues, suggestions, or questions:

- Open an issue on GitHub
- Check existing issues for solutions
- See the [WIKI](WIKI.md) for common questions

## 🎓 Learning Resources

- [Minecraft Data Packs](https://minecraft.fandom.com/wiki/Data_pack)
- [Minecraft Resource Packs](https://minecraft.fandom.com/wiki/Resource_pack)
- [NBT Format](https://minecraft.fandom.com/wiki/NBT_format)
- [NeoForge Documentation](https://docs.neoforged.net/)

## 🙏 Credits

Developed for Minecraft 1.21.1 using NeoForge.

Special thanks to the Minecraft modding community for inspiration and feedback.

## 📦 Mod Information

- **Name**: Villager Workshops
- **Version**: 1.0.0
- **MC Version**: 1.21.1
- **Mod Loader**: NeoForge
- **Java Version**: 21+

---

**Ready to create custom professions?** Start with [QUICKSTART.md](QUICKSTART.md)! 🚀
