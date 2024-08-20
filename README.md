# Minecraft 'Coordinates Book' Mod

![image](https://github.com/ItsNotCam/MC-Coordinates-Book/assets/46014191/bc39f9a3-2ee5-4501-ae85-6be18907bd68)

## Overview
This is a multiplayer mod (called a "plugin") for the popular video game **Minecraft**. This mod allows you to **save**, **view**, and even **share with your friends** your **favorite locations** within the game. It does this all with a **sleek user interface** that is integrated seamlessly with the rest of the game's interfaces such that **it feels as if it was a standard feature**.

## Why
The game is **massive** - each world can be up to **60 million square meters** which is about **five times the diameter of the earth**. As such, it can be difficult to keep track of your favorite locations. 

The **traditional** method of saving these locations is to:
1. Press "f3" to pull up the in-game **debug menu** (yes, the menu used for developers)
2. **Locate** the three numbers (X, Y, Z coordinates) that define your **player position** 
3. Note these coordinates down **somewhere** *Don't forget to save that notepad document... and put it in a place you won't forget it... good luck*

Not only does opening the debug menu **"break the fourth wall"**, it also (due to the other information located within the debug menu) is **considered cheating** by parts of the community. Not only that but performing all of those steps is also **just a lot of work**, and I am lazy; I had to **automate it**.

## Limitations
There are a lot of limitations in making multiplayer plugins for the vanilla Minecraft client and server experience. Multiplayer plugins are **not allowed to add any new content to the game**. Plugins are only permitted to **modify what already exists**. As such, plugin creators like myself have to get creative in order to make an experience using only existing assets.

## Features
When using this plugin you can:
* **Save** your current location to a persistent database
* **Instantly travel** (teleport) to any location you have saved
* Set your in-game **compass to point** to any location that you have saved
* **Share** any location you have saved **with any other player** on the server using a **very intuitive interface**

## Technology Used
**Java and [Minecraft Spigot SDK](https://www.spigotmc.org)**

* Java is the language the game client and server are written in
* The Spigot SDK is a third party SDK that allows developers to easily create their own addons and plugins. 


**SQLite**

* **Not every server owner** has access to a traditional cloud-based database, or even full access to the physical server that they run their world on. 
* I had to make use of SQLite, as it **only relies on a local database file**, rather than a dedicated database application - **this makes it perfect for all users**.

## What I Learned
In making this project, **I learned a lot about Java**. Prior to making this plugin, I was not a big fan of Java and upon seeing that I *had* to use Java, I was a bit put off. But after creating this I can say that **I actually really enjoyed working with Java** :)

Additionally, **I learned about SQLite databases**. This was my **first exposure to databases**, and I taught myself the fundamentals.