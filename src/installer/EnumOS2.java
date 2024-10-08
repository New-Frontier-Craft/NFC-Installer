package installer;

public enum EnumOS2
{
    linux("linux", 0),
    solaris("solaris", 1),
    windows("windows", 2),
    macos("macos", 3),
    unknown("unknown", 4);

    private EnumOS2(String s, int i)
    {

    }

    @SuppressWarnings("unused")
	private static final EnumOS2 field_6511_f[];

    static 
    {
        field_6511_f = (new EnumOS2[] {
            linux, solaris, windows, macos, unknown
        });
    }
}
