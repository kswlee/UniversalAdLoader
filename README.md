[![](https://jitpack.io/v/kswlee/UniversalAdLoader.svg)](https://jitpack.io/#kswlee/UniversalAdLoader)

# UniveralAdLoader - An Android local Ad mediation helper

Mix variant Ad unit with different formats (e.g. Banner, NativeBanner, Interstital) in a single ad space to facilitat the Ad loading waterflow. 

### Howto 
- Define the ad units list. (Refer to AdUnits.kt in the project)
- Call AdLoader.loadByOrder(adUnits) to load ad (Refer to MainActivity in the project)
- loadByOrder returns the Rx Observable to emit loaded wrapped Ad instance 
