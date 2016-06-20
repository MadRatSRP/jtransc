package java.net;

public class Proxy {

	/**
	 * Represents the proxy type setting {@code Proxy.Type.DIRECT}. It tells
	 * protocol handlers that there is no proxy to be used. The address is set
	 * to {@code null}.
	 */
	public static final Proxy NO_PROXY = new Proxy();

	private Proxy.Type type;

	private SocketAddress address;

	/**
	 * Creates a new {@code Proxy} instance. {@code SocketAddress} must NOT be
	 * {@code null} when {@code type} is either {@code Proxy.Type.HTTP} or
	 * {@code Proxy.Type.SOCKS}. To create a {@code Proxy} instance representing
	 * the proxy type {@code Proxy.Type.DIRECT}, use {@code Proxy.NO_PROXY}
	 * instead of this constructor.
	 *
	 * @param type
	 *            the proxy type of this instance.
	 * @param sa
	 *            the proxy address of this instance.
	 * @throws IllegalArgumentException
	 *             if the parameter {@code type} is set to {@code
	 *             Proxy.Type.DIRECT} or the value for {@code SocketAddress} is
	 *             {@code null}.
	 */
	public Proxy(Proxy.Type type, SocketAddress sa) {
        /*
         * Don't use DIRECT type to construct a proxy instance directly.
         * SocketAddress must NOT be null.
         */
		if (type == Type.DIRECT || sa == null) {
			throw new IllegalArgumentException("Illegal Proxy.Type or SocketAddress argument");
		}
		this.type = type;
		address = sa;
	}

	/*
     * Constructs a Proxy instance, which is Proxy.DIRECT type with null
     * SocketAddress. This constructor is used for NO_PROXY.
     */
	private Proxy() {
		type = Type.DIRECT;
		address = null;
	}

	/**
	 * Gets the type of this {@code Proxy} instance.
	 *
	 * @return the stored proxy type.
	 */
	public Proxy.Type type() {
		return type;
	}

	/**
	 * Gets the address of this {@code Proxy} instance.
	 *
	 * @return the stored proxy address or {@code null} if the proxy type is
	 *         {@code DIRECT}.
	 */
	public SocketAddress address() {
		return address;
	}

	/**
	 * Gets a textual representation of this {@code Proxy} instance. The string
	 * includes the two parts {@code type.toString()} and {@code
	 * address.toString()} if {@code address} is not {@code null}.
	 *
	 * @return the representing string of this proxy.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (type != null) {
			builder.append(type.toString());
		}
		builder.append("@");
		if (type != Proxy.Type.DIRECT && address != null) {
			builder.append(address.toString());
		}
		return builder.toString();
	}

	/**
	 * Compares the specified {@code obj} to this {@code Proxy} instance and
	 * returns whether they are equal or not. The given object must be an
	 * instance of {@code Proxy} with the same address and the same type value
	 * to be equal.
	 *
	 * @param obj
	 *            the object to compare with this instance.
	 * @return {@code true} if the given object represents the same {@code
	 *         Proxy} as this instance, {@code false} otherwise.
	 * @see #hashCode
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Proxy)) {
			return false;
		}
		Proxy another = (Proxy) obj;
		// address is null when and only when it's NO_PROXY.
		return (type == another.type) && address.equals(another.address);
	}

	/**
	 * Gets the hashcode for this {@code Proxy} instance.
	 *
	 * @return the hashcode value for this Proxy instance.
	 */
	@Override
	public final int hashCode() {
		int ret = 0;
		ret += type.hashCode();
		if (address != null) {
			ret += address.hashCode();
		}
		return ret;
	}

	/**
	 * {@code Enum} class for the proxy type. Possible options are {@code
	 * DIRECT}, {@code HTTP} and {@code SOCKS}.
	 */
	public enum Type {
		/**
		 * Direct connection. Connect without any proxy.
		 */
		DIRECT,

		/**
		 * HTTP type proxy. It's often used by protocol handlers such as HTTP,
		 * HTTPS and FTP.
		 */
		HTTP,

		/**
		 * SOCKS type proxy.
		 */
		SOCKS
	}
}