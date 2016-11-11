package com.teamdman.animus.client.resources;
import com.teamdman.animus.Animus;


import WayofTime.bloodmagic.api.util.helper.*;


public class Loader {

    private Loader() {}
    


    protected static BindableResource load(AssetLocation location, SubLocation subLocation, String name, String suffix) {
        return new BindableResource(buildResourceString(location, subLocation, name, suffix));
    }

    private static String buildResourceString(AssetLocation location, SubLocation subLocation, String name, String suffix) {
    	LogHelper logger = new LogHelper("Animus Debug");
    	if(name.endsWith(suffix)) { //In case of derp.
            name = name.substring(0, name.length() - suffix.length());
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Animus.MODID).append(':').append(location.location).append("/");
        if (subLocation != null) {
            builder.append(subLocation.getLocation()).append("/");
        }
        builder.append(name).append(suffix);
        logger.fatal("location: " + builder.toString());
        return builder.toString();
    }

    protected static BindableResource loadTexture(TextureLocation location, String name) {
        return load(AssetLocation.TEXTURES, location, name, ".png");
    }


    public static interface SubLocation {

        public String getLocation();

    }

    public static enum ModelLocation implements SubLocation {

        OBJ("obj");

        private final String location;

        private ModelLocation(String location) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return location;
        }

    }

    public static enum TextureLocation implements SubLocation {

        ITEMS("items"),
        BLOCKS("blocks"),
        GUI("gui"),
        MISC("misc"),
        MODELS("models"),
        EFFECT("effect"),
        ENVIRONMENT("environment");

        private final String location;

        private TextureLocation(String location) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return location;
        }

    }

    public static enum AssetLocation {

        MODELS("models"),
        TEXTURES("textures");

        private final String location;

        private AssetLocation(String location) {
            this.location = location;
        }

    }

}
