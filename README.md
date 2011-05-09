SVS
===

Warning : this is an alpha release. It works but we can add a bunch of new features ;). 
Please feel free fork and contribute.

SVS (Small Version System) is a small versioning tool to handle different version of
java object in memory. This tool tool provide:

* A small memory print
* An easy to use API to make snapshots
* Easy way to load and save versions of an object/structure.

Repository usage
================

How it works ? Just create a SVSRepository

	SVSRepository<MySerializableObject> repository = new SVSRepositoryImpl<MySerializableObject>();
  
make a snapshot

	String myRev = repository.makeSnapshot(myObject);
	
and restore it
	 
	MySerializableObject object = repository.restoreSnapshot(myRev);
	
You can also patch your work ;)

	SVSRepository<String> repository = new SVSRepositoryImpl<String>();

	String rev1 = repository.makeSnapshot("Wow");
	String expanded = repository.makeSnapshot("World of Warcraft");
	repository.makeSnapshot("World of Warcraft\n2");
	String expandedWow3Hash = repository.makeSnapshot("World of Warcraft\n3");
	repository.makeSnapshot("Wow\n3");

	// create patch -Wow +World of Warcraft
	SVSPatch<String> patch = repository.getSVSPatchBeetween(rev1, expanded);

	// try to apply it on "Wow 3"
	repository.applyPatch(patch);
	assertEquals("World of Warcraft\n3", repository.getLatestSnapshot());

	// verify that hash is identical to previous histo "World of Warcraft 3"
	assertEquals(expandedWow3Hash, repository.getLatestRevNumber());
	
Take a look at unit tests to see all possibilities of the library. 

Patcher usage
=============

Small usage of "patching" possibilies

		SVSPatcher<Person> patcher = new SVSPatcher<Person>();

		// first
		Person p = new Person();
		p.setName("Bob");
		p.setAge(17);
		p.setTel("1545645646");
		p.setAdress("3 rue du gymnase\n89245 Bidonville");

		// modified person
		Person p1 = new Person();
		p1.setName("Bob");
		p1.setAge(18);
		p1.setTel("33355566");
		p1.setAdress("3 rue du gymnase\n33333 Bidonville");

		// patch
		SVSPatch<Person> patch = patcher.makeSVSPatchFor(p, p1);

		// object to patch (slightly different)
		Person pToPatch = new Person();
		pToPatch.setName("Bob José");
		pToPatch.setAge(17);
		pToPatch.setTel("1545645646");
		pToPatch.setAdress("9 rue du gymnase\n89245 Bidonville");

		Person patchedPerson = patcher.patchWith(pToPatch, patch);

		assertEquals("33355566", patchedPerson.getTel());
		assertEquals(18, patchedPerson.getAge());
		assertEquals("9 rue du gymnase\n33333 Bidonville",
				patchedPerson.getAdress());

