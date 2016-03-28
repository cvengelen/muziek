//
// Project:	muziek
// File:	OpnameKey.java
// Description:	Key of record in opname
// Author:	Chris van Engelen
// History:	2005/05/01: Initial version
//

package muziek.gui;

public class OpnameKey {
    private int mediumId = 0;
    private int opusId = 0;
    private int opnameNummer = 0;
    private int musiciId = 0;

    public OpnameKey( ) {
	this.mediumId = 0;
	this.opusId = 0;
	this.opnameNummer = 0;
	this.musiciId = 0;
    }

    public OpnameKey( int mediumId,
		      int opusId,
		      int opnameNummer,
		      int musiciId ) {
	this.mediumId = mediumId;
	this.opusId = opusId;
	this.opnameNummer = opnameNummer;
	this.musiciId = musiciId;
    }

    public int getMediumId( ) { return mediumId; }

    public int getOpusId( ) { return opusId; }

    public int getOpnameNummer( ) { return opnameNummer; }

    public int getMusiciId( ) { return musiciId; }

    public boolean equals( Object object ) {
	if ( object == null ) return false;
	if ( ! ( object instanceof OpnameKey ) ) return false;
	if ( ( ( ( OpnameKey )object ).getMediumId( )     != mediumId     ) ||
	     ( ( ( OpnameKey )object ).getOpusId( )       != opusId       ) ||
	     ( ( ( OpnameKey )object ).getOpnameNummer( ) != opnameNummer ) ||
	     ( ( ( OpnameKey )object ).getMusiciId( )     != musiciId     ) ) return false;
	return true;
    }
    public String toString( ) {
	return ( "medium_id: " + mediumId +
		 ", opus_id: " + opusId +
		 ", opname_nummer: " + opnameNummer +
		 ", musici_id: " + musiciId );
    }
}
