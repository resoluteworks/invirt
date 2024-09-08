import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
    {
        title: 'Making the most of Kotlin',
        Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
        description: (
            <>
                Invirt is build in entirely in Kotlin (JVM) and makes the most of
                the language capabilities to deliver fluent and type-safe approaches
                to building web applications.
            </>
        ),
    },
    {
        title: 'Developer experience as a key goal',
        Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
        description: (
            <>
                Invirt provides a series of tools and utilities to make bootstrapping
                your application as easy and quick as possible, and your development process
                frictionless.
            </>
        ),
    }
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col')}>
            <div className="text--center">
                <Svg className={styles.featureSvg} role="img"/>
            </div>
            <div className="text--center padding-horiz--md">
                <Heading as="h3">{title}</Heading>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div>
                    <img class="no-img-styling" src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Finvirt%2Finvirt-core%2Fmaven-metadata.xml&style=for-the-badge&label=RELEASE&color=%23008899"/>
                </div>
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
