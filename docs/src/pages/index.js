import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import Heading from '@theme/Heading';
import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
          <Heading as="h1" className="hero__title">
              Invirt is a framework for building web applications with <span className="title-inline-ref">Kotlin</span>, <span className="title-inline-ref">Http4k</span> and <span className="title-inline-ref">Pebble</span> templates.
          </Heading>
          <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg main-cta"
            to="/docs/overview/quickstart">
            Get started
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
